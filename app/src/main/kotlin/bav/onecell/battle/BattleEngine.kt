package bav.onecell.battle

import bav.onecell.model.BattleFieldSnapshot
import bav.onecell.model.BattleInfo
import bav.onecell.model.GameRules
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.BattleFieldState
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import bav.onecell.model.hexes.Layout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.launch
import java.util.LinkedList
import java.util.Queue
import java.util.Random
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.round

class BattleEngine(
        private val hexMath: HexMath,
        private val gameRules: GameRules,
        private val cellRepository: RepositoryContract.CellRepo) {

    companion object {
        private const val TAG = "BattleEngine"
        private const val DEATH_RAY_DISTANCE = 50
        private const val DEATH_RAY_DAMAGE = 10
    }

    private val cells = mutableListOf<Cell>()
    private val corpses = mutableListOf<Cell>()
    private var battleFieldSize: Int = 0
    private val battleState = BattleFieldState()
    private val battleFieldSnapshots = mutableListOf<BattleFieldSnapshot>()
    private lateinit var currentSnapshot: BattleFieldSnapshot
    val battleResultProvider = PublishSubject.create<BattleInfo>()
    private val damageDealtByCells: MutableMap<Int, Int> = mutableMapOf()
    private var cellRepositoryDisposable: Disposable? = null
    private var isFog: Boolean = false

    //region Partial round steps
    private val battleRoundSteps: Queue<() -> Unit> = LinkedList<() -> Unit>()

    private lateinit var firstStep: () -> Unit

    private val calculateBattleState = { calculateBattleState() }

    private val applyCellsLogic = { cells.forEachIndexed { index, cell -> applyCellLogic(index, cell) } }

    private val moveCells = { moveCells() }

    private val calculateDamages = {
        for (i in 0 until cells.size) { currentSnapshot.hexesToRemove.add(mutableListOf()) }
        checkDeathRays()
        checkIntersections()
        checkNeighboring()
    }

    private val checkWhetherBattleEnds = {
        if (isBattleOver()) {
            saveSnapshot()
        }
    }
    //endregion

    init {
        initializeBattleSteps()
    }

    fun initialize(cellIndexes: List<Int>, useFog: Boolean = false) {
        isFog = useFog
        clearEngine()
        cellRepositoryDisposable?.dispose()
        cellRepositoryDisposable = cellRepository.loadFromStore()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { startCalculation(cellIndexes) }
    }

    private fun startCalculation(cellIndexes: List<Int>) {
        // Make copy of cells
        for (i in cellIndexes) cellRepository.getCell(i)?.let {
            val clone = it.clone()
            clone.battleData.battleId = i
            clone.battleData.isAlive = true
            damageDealtByCells[i] = 0
            clone.evaluateCellHexesPower()
            clone.updateOutlineHexes()
            cells.add(clone)
        }

        battleFieldSize = round(cells.asSequence().map { it.size() }.sum() * 1.5).toInt()

        moveCellsToTheirInitialPosition()
        saveCellsAndCorpsesToSnapshot()
        evaluateBattle()
    }

    //region Private methods
    private fun getDeadOrAliveCells(snapshot: BattleFieldSnapshot): Map<Int, Boolean> {
        val deadOrAliveCells = mutableMapOf<Int, Boolean>()
        for (cell in snapshot.cells) deadOrAliveCells[cell.battleData.battleId] = cell.battleData.isAlive
        for (corpse in snapshot.corpses) deadOrAliveCells[corpse.battleData.battleId] = corpse.battleData.isAlive
        return deadOrAliveCells
    }

    private fun clearEngine() {
        cells.clear()
        corpses.clear()
        battleFieldSnapshots.clear()
        damageDealtByCells.clear()
        currentSnapshot = BattleFieldSnapshot()
    }

    private fun initializeBattleSteps() {
        firstStep = calculateBattleState
        for (action in arrayOf(
                calculateBattleState,
                applyCellsLogic,
                moveCells,
                calculateDamages,
                checkWhetherBattleEnds)
        ) battleRoundSteps.add(action)
    }

    private fun evaluateBattle() {
        launch {
            while (!isBattleOver()) {
                doFullStep()
            }
            battleResultProvider.onNext(BattleInfo(battleFieldSnapshots, damageDealtByCells,
                                                   getDeadOrAliveCells(battleFieldSnapshots.last()),
                                                   isFog))
        }
    }

    private fun doFullStep() {
        do {
            doPartialStep()
        } while (battleRoundSteps.peek() != firstStep)
        saveSnapshot()
    }

    private fun doPartialStep() {
        // Get next action
        val action = battleRoundSteps.poll()
        action.invoke()
        battleRoundSteps.add(action)
    }

    private fun createNextSnapshot() {
        currentSnapshot = BattleFieldSnapshot()
    }

    private fun saveCellsAndCorpsesToSnapshot() {
        // TODO: save only viewable data (i.e. rules aren't need to be cloned)
        for (c in cells) {
            currentSnapshot.cells.add(c.clone())
        }
        for (c in corpses) {
            currentSnapshot.corpses.add(c.clone())
        }
    }

    private fun saveSnapshot() {
        battleFieldSnapshots.add(currentSnapshot)
        createNextSnapshot()
        saveCellsAndCorpsesToSnapshot()
    }

    private fun moveCellsToTheirInitialPosition() {
        val ringRadius = battleFieldSize - (cells.asSequence().map { it.size() }.max() ?: 0)
        val ring = mutableListOf<Hex>()
        var hex = hexMath.multiply(hexMath.getHexByDirection(4), ringRadius)
        for (i in 0..5) {
            for (j in 0..(ringRadius - 1)) {
                ring.add(hex)
                hex = hexMath.getHexNeighbor(hex, i)
            }
        }
        val step = ring.size / cells.size
        cells.forEachIndexed { index, cell -> cell.data.origin = ring[index * step] }
    }

    private fun calculateBattleState() {
        battleState.clear()

        // Each cell will move to the nearest hex within all enemies.
        // Distance will be calculated between all hexes of cell and all enemy hexes.
        cells.forEachIndexed { i, cell ->
            var ourHex: Hex = cell.data.origin
            var nearestEnemyHex: Hex = cell.data.origin
            var minDistance = Int.MAX_VALUE
            // Search for nearest enemy hex
            cell.data.hexes.forEach { entry ->
                val ourCandidate = hexMath.add(entry.value, cell.data.origin)
                cells.forEachIndexed { j, enemy ->
                    if (j != i && cell.data.groupId != enemy.data.groupId) {
                        enemy.data.hexes.forEach {
                            val enemyCandidate = hexMath.add(it.value, enemy.data.origin)
                            val distance = hexMath.distance(ourCandidate, enemyCandidate)
                            if (distance < minDistance) {
                                minDistance = distance
                                ourHex = ourCandidate
                                nearestEnemyHex = enemyCandidate
                            }
                        }
                    }
                }
            }
            // Find direction to move
            // Origin point
            val op = hexMath.hexToPixel(Layout.DUMMY, ourHex)
            // Nearest enemy hex point
            val nehp = hexMath.hexToPixel(Layout.DUMMY, nearestEnemyHex)
            // Distance to the enemy hex
            battleState.distances.add(hexMath.distance(ourHex, nearestEnemyHex))
            // Angle direction to enemy hex
            val angle = atan2(nehp.y.toFloat() - op.y.toFloat(), nehp.x.toFloat() - op.x.toFloat())
            battleState.rads.add(angle)
            // Determine direction based on angle
            battleState.directions.add(hexMath.radToNeighborDirection(angle))
        }
    }

    private fun applyCellLogic(index: Int, cell: Cell) {
        correctBattleStateForCell(index, cell)
        val performedAction = cell.applyCellLogic(battleState)
        if (performedAction != null) {
            // TODO: currently it's applicable only for rotations
            cell.updateOutlineHexes()
        }
        currentSnapshot.cellsActions.add(performedAction)
    }

    private fun correctBattleStateForCell(index: Int, cell: Cell) {
        // In case of fog and enemy is not seen we should say cell that enemy is far away and in random direction.
        if (isFog && battleState.distances[index] > cell.data.viewDistance) {
            battleState.directionToNearestEnemy = Cell.Direction.fromInt((0..5).shuffled().last())
            battleState.distanceToNearestEnemy = Int.MAX_VALUE
        } else {
            // Enemy is seen clearly
            battleState.directionToNearestEnemy = radToCellDirection(battleState.rads[index])
            battleState.distanceToNearestEnemy = battleState.distances[index]
        }
    }

    //    N
    // NW /\ NE
    //   |  |
    // SW \/ SE
    //    S
    private fun radToCellDirection(angle: Float): Cell.Direction {
        if (angle < -PI.toFloat() || angle > PI.toFloat()) throw IllegalArgumentException(
                "Angle should be in range [-PI..PI]")
        return if (angle >= (-PI * 2) && angle < (-PI * 2 / 3)) Cell.Direction.NW
        else if (angle >= (-PI * 2 / 3) && angle < (-PI / 3)) Cell.Direction.N
        else if (angle >= (-PI / 3) && angle < 0) Cell.Direction.NE
        else if (angle >= 0 && angle < (PI / 3)) Cell.Direction.SE
        else if (angle >= (PI / 3) && angle < (PI * 2 / 3)) Cell.Direction.S
        else Cell.Direction.SW
    }

    private fun moveCells() {
        // Move cells
        battleState.directions.forEachIndexed { index, direction ->
            currentSnapshot.movingDirections.add(direction)
            cells[index].data.origin = hexMath.add(cells[index].data.origin, hexMath.getHexByDirection(direction))
        }
    }

    private fun checkDeathRays() {
        cells.groupBy { it.data.groupId }.values.forEach { groupOfCells ->
            if (groupOfCells.size < 2) return@forEach
            val currentGroupId = groupOfCells[0].data.groupId
            val listOfDeathRayHexes = groupOfCells.asSequence()
                    .map { cell -> cell.data.hexes.values
                            .filter { hex -> hex.type == Hex.Type.DEATH_RAY }
                            .map { hexMath.add(it, cell.data.origin) }
                    }.toMutableList()
            val deathRays = mutableListOf<Pair<Hex, Hex>>()
            while (listOfDeathRayHexes.size > 1) {
                val deathRayHexesOfOneCell = listOfDeathRayHexes.first()
                listOfDeathRayHexes.removeAt(0)
                deathRayHexesOfOneCell.forEach { startHex ->
                    listOfDeathRayHexes.forEach { endCell ->
                        endCell.forEach { endHex ->
                            if (hexMath.distance(startHex, endHex) <= DEATH_RAY_DISTANCE) {
                                deathRays.add(Pair(startHex, endHex))
                            }
                        }
                    }
                }
            }
            // Пройтись по всем лучам, получить хексы, входящие в лучи
            val hexesAffectedByRays = mutableSetOf<Hex>()
            deathRays.forEach { hexesAffectedByRays.addAll(hexMath.lineDraw(it.first, it.second)) }
            // Пройтись по всем вражеским клеткам и нанести им урон лучами
            cells.filter { it.data.groupId != currentGroupId }.forEach { enemy ->
                enemy.data.hexes.values
                        .filter { hexesAffectedByRays.contains(hexMath.add(enemy.data.origin, it)) }
                        .forEach { it.power -= DEATH_RAY_DAMAGE }
            }
        }
        checkCellsVitality()
    }

    private fun checkIntersections() {
        // Get intersection of all cells
        val allHexes = mutableSetOf<Hex>()
        val intersectedHexes = mutableSetOf<Hex>()
        cells.forEach { cell ->
            cell.data.hexes.forEach { entry ->
                val hexInGlobalCoords = hexMath.add(cell.data.origin, entry.value)
                if (!allHexes.add(hexInGlobalCoords)) {
                    intersectedHexes.add(hexInGlobalCoords)
                }
            }
        }
        // Deal damage to each intersected hex
        intersectedHexes.forEach { intersected ->
            // Calculate maximum damages dealt by hexes with same group id
            val maxDamageOfGroup = mutableMapOf<Int, Int>()
            cells.forEach { cell ->
                // intersected are in global coordinates, we should revert them to local ones
                cell.data.hexes[hexMath.subtract(intersected, cell.data.origin).hashCode()]?.let { hex ->
                    val currentGroupDamage = maxDamageOfGroup.getOrPut(cell.data.groupId) { 0 }
                    if (hex.power > currentGroupDamage) {
                        maxDamageOfGroup[cell.data.groupId] = hex.power
                    }
                }
            }
            // Deal damage
            cells.forEach { cell ->
                cell.data.hexes[hexMath.subtract(intersected, cell.data.origin).hashCode()]?.let {
                    // Each hex, if it is in intersection, receives damage equals to maximum of damages within
                    // enemy's groups.
                    val currentDamage = damageDealtByCells[cell.battleData.battleId] ?: 0
                    damageDealtByCells[cell.battleData.battleId] = currentDamage + it.power
                    it.power -= maxDamageOfGroup
                            .filter { entry -> entry.key != cell.data.groupId }
                            .maxBy { entry -> entry.value }?.value ?: 0
                }
            }
        }
        // After dealing damage we have to check cells for vitality
        checkCellsVitality()
    }

    private fun checkNeighboring() {
        cells.forEach { cell ->
            // Get cell outline
            val cellOutline = cell.getOutlineHexes().map { hexMath.add(cell.data.origin, it) }
            // Get all enemy hexes which are neighbors to us
            val neighboringEnemyHexesByBattleId = mutableMapOf<Int, MutableSet<Hex>>()
            cells.filter { it.data.groupId != cell.data.groupId }.forEach { enemy ->
                neighboringEnemyHexesByBattleId.getOrPut(enemy.battleData.battleId) { mutableSetOf() }.addAll(
                        enemy.data.hexes.values.map { hexMath.add(enemy.data.origin, it).withPower(it.power) }
                                .intersect(cellOutline))
            }
            // Come through all our hexes
            /// TODO: no need to run over all our hexes, only cell border is necessary
            cell.data.hexes.values.forEach { ourHex ->
                val hexInGlobal = hexMath.add(ourHex, cell.data.origin)
                neighboringEnemyHexesByBattleId.forEach { enemyHexes ->
                    val neighbors = enemyHexes.value.intersect(hexMath.hexNeighbors(hexInGlobal))
                    // Saving damage to our hex
                    neighbors.forEach {
                        ourHex.receivedDamage += it.power
                        // Save dealt damage
                        val currentDamage = damageDealtByCells[enemyHexes.key] ?: 0
                        damageDealtByCells[enemyHexes.key] = currentDamage + it.power
                    }
                }
            }
        }
        // Deal real damage
        cells.forEach { cell ->
            cell.data.hexes.values.forEach { hex ->
                if (hex.receivedDamage > 0) {
                    hex.power -= hex.receivedDamage
                    hex.receivedDamage = 0
                }
            }
        }
        checkCellsVitality()
    }

    private fun checkCellsVitality() {
        val cellsToUpdateOutline = mutableListOf<Int>()
        val cellsToRemove = mutableListOf<Int>()
        cells.forEachIndexed { index, cell ->
            // Remove powerless hexes
            val hexesToRemove = mutableListOf<Hex>()
            cell.data.hexes.forEach { entry ->
                // If power becomes less or equal then zero, hex should be removed from cell
                if (entry.value.power <= 0) {
                    hexesToRemove.add(entry.value)
                }
            }

            currentSnapshot.hexesToRemove[index].addAll(correctHexesToRemoveForSnapshot(hexesToRemove, index))

            if (hexesToRemove.isNotEmpty()) {
                hexesToRemove.forEach { cell.data.hexes.remove(it.hashCode()) }
                // If connectivity of life and energy hexes has been broken then cell dies
                if (!gameRules.checkHexesConnectivity(cell.data.hexes.values.filter {
                            it.type == Hex.Type.LIFE || it.type == Hex.Type.ENERGY
                        }) ||
                        // Cells also dies if it doesn't contain any life hexes
                        cell.data.hexes.values.filter { it.type == Hex.Type.LIFE }.isEmpty()) {
                    cellsToRemove.add(index)
                } else {
                    cellsToUpdateOutline.add(index)
                }
            }
        }
        cellsToUpdateOutline.sortDescending()
        cellsToUpdateOutline.forEach {
            cells[it].updateOutlineHexes()
        }
        cellsToRemove.sortDescending()
        cellsToRemove.forEach {
            corpses.add(cells[it])
            cells[it].battleData.isAlive = false
            cells.removeAt(it)
        }
    }

    private fun isBattleOver(): Boolean = cells.map { it.data.groupId }.distinct().size <= 1

    private fun correctHexesToRemoveForSnapshot(hexesToRemove: List<Hex>, cellIndex: Int): Collection<Int> {
        val snapshotCell = currentSnapshot.cells[cellIndex]
        val snapshotCellDirection = snapshotCell.data.direction
        val newDirection = cells[cellIndex].data.direction

        return if (snapshotCellDirection == newDirection) hexesToRemove.map { it.hashCode() }
        else hexesToRemove.map { snapshotCell.rotateHex(it, newDirection, snapshotCellDirection).hashCode() }
    }
    //endregion
}
