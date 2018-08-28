package bav.onecell.battle

import android.util.Log
import bav.onecell.model.BattleFieldSnapshot
import bav.onecell.model.BattleInfo
import bav.onecell.model.GameRules
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.BattleFieldState
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import bav.onecell.model.hexes.Layout
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.launch
import java.util.LinkedList
import java.util.Queue
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.round

class BattleEngine(
        private val hexMath: HexMath,
        private val gameRules: GameRules,
        private val cellRepository: RepositoryContract.CellRepo) {

    companion object {
        private const val TAG = "BattleEngine"
    }

    private val cells = mutableListOf<Cell>()
    private val corpses = mutableListOf<Cell>()
    private var battleFieldSize: Int = 0
    private val battleState = BattleFieldState()
    private val battleFieldSnapshots = mutableListOf<BattleFieldSnapshot>()
    private lateinit var currentSnapshot: BattleFieldSnapshot
    val battleResultProvider = PublishSubject.create<BattleInfo>()

    //region Partial round steps
    private val battleRoundSteps: Queue<() -> Unit> = LinkedList<() -> Unit>()

    private lateinit var firstStep: () -> Unit

    private val calculateBattleState = { calculateBattleState() }

    private val applyCellsLogic = { cells.forEachIndexed { index, cell -> applyCellLogic(index, cell) } }

    private val moveCells = { moveCells() }

    private val calculateDamages = {
        for (i in 0 until cells.size) { currentSnapshot.hexesToRemove.add(mutableListOf()) }
        checkIntersections()
        checkNeighboring()
    }

    private val checkWhetherBattleEnds = {
        if (isBattleOver()) {
            saveSnapshot()
            battleResultProvider.onNext(BattleInfo(battleFieldSnapshots))
        }
    }
    //endregion

    init {
        initializeBattleSteps()
    }

    fun initialize(cellIndexes: List<Int>) {
        clearEngine()

        // Make copy of cells
        for (i in cellIndexes) cellRepository.getCell(i)?.let {
            val clone = it.clone()
            clone.evaluateCellHexesPower()
            clone.updateOutlineHexes()
            cells.add(clone)
        }

        battleFieldSize = round(cells.map { it.size() }.sum() * 1.5).toInt()

        moveCellsToTheirInitialPosition()
        saveCellsAndCorpsesToSnapshot()
        evaluateBattle()
    }

    //region Private methods
    private fun clearEngine() {
        cells.clear()
        corpses.clear()
        battleFieldSnapshots.clear()
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
        val ringRadius = battleFieldSize - (cells.map { it.size() }.max() ?: 0)
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
        battleState.directions.clear()
        battleState.rads.clear()

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
                    if (j != i) {
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
            // Angle direction to enemy hex
            val angle = atan2(nehp.y.toFloat() - op.y.toFloat(), nehp.x.toFloat() - op.x.toFloat())
            battleState.rads.add(angle)
            // Determine direction based on angle
            battleState.directions.add(hexMath.radToNeighborDirection(angle))
        }
    }

    private fun applyCellLogic(index: Int, cell: Cell) {
        correctBattleStateForCell(index)
        val performedAction = cell.applyCellLogic(battleState)
        if (performedAction != null) {
            // TODO: currently it's applicable only for rotations
            cell.updateOutlineHexes()
        }
        currentSnapshot.cellsActions.add(performedAction)
    }

    private fun correctBattleStateForCell(index: Int) {
        battleState.directionToNearestEnemy = radToCellDirection(battleState.rads[index])
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
            // Calculate second in strength of power value for each intersected hex
            var damage = 0
            var maxPower = 0
            cells.forEach { cell ->
                // intersected are in global coordinates, we should revert them to local ones
                cell.data.hexes[hexMath.subtract(intersected, cell.data.origin).hashCode()]?.let {
                    if (it.power >= maxPower) {
                        damage = maxPower
                        maxPower = it.power
                    } else if (it.power > damage) {
                        damage = it.power
                    }
                }
            }
            // Deal damage
            cells.forEach { cell ->
                cell.data.hexes[hexMath.subtract(intersected, cell.data.origin).hashCode()]?.let {
                    it.power -= damage
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
            // Get all enemy hexes
            val enemyHexes = mutableSetOf<Hex>()
            cells.filter { it != cell }.forEach { enemy ->
                enemyHexes.addAll(
                        enemy.data.hexes.values.map { hexMath.add(enemy.data.origin, it).withPower(it.power) })
            }
            // Get all enemy hexes which are neighbors to us
            val neighboringHexes = enemyHexes.intersect(cellOutline)
            // Come through all our hexes
            cell.data.hexes.values.forEach { hex ->
                val hexInGlobal = hexMath.add(hex, cell.data.origin)
                val neighbors = neighboringHexes.intersect(hexMath.hexNeighbors(hexInGlobal))
                // Saving damage to our hex
                neighbors.forEach { hex.receivedDamage += it.power }
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
            val hexesToRemove = mutableListOf<Int>()
            cell.data.hexes.forEach { entry ->
                // If power becomes less or equal then zero, hex should be removed from cell
                if (entry.value.power <= 0) {
                    hexesToRemove.add(entry.key)
                }
            }
            currentSnapshot.hexesToRemove[index].addAll(hexesToRemove)
            if (hexesToRemove.isNotEmpty()) {
                hexesToRemove.forEach { cell.data.hexes.remove(it) }
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
            cells.removeAt(it)
        }
    }

    private fun isBattleOver(): Boolean = cells.size <= 1
    //endregion
}
