package bav.onecell.battle

import bav.onecell.model.RepositoryContract
import bav.onecell.model.GameRules
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.BattleState
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import bav.onecell.model.hexes.Layout
import java.util.LinkedList
import java.util.Queue
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.round

/// TODO: move calculations to background thread
/// TODO: produce sequence of battle steps and provide them via RX
/// TODO: add visible representation of count of calculated steps with cursor, showing current step

class BattlePresenter(
        private val view: Battle.View,
        private val hexMath: HexMath,
        private val gameRules: GameRules,
        private val cellRepository: RepositoryContract.CellRepo) : Battle.Presenter {

    companion object {
        private const val TAG = "BattlePresenter"
    }

    private val battleRoundSteps: Queue<() -> Unit> = LinkedList<() -> Unit>()
    private val cells = mutableListOf<Cell>()
    private val corpses = mutableListOf<Cell>()
    private var battleFieldSize: Int = 0
    private val battleState = BattleState()

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

    override fun initialize(cellIndexes: List<Int>) {
        initializeBattleSteps()

        // Make copy of cells
        for (i in cellIndexes) cellRepository.getCell(i)?.let {
            val clone = it.clone()
            clone.evaluateCellHexesPower()
            clone.updateOutlineHexes()
            cells.add(clone)
        }

        battleFieldSize = round(cells.map { it.size() }.sum() * 1.5).toInt()

        view.setBackgroundFieldRadius(battleFieldSize)
        view.setCells(cells)
        view.setCorpses(corpses)

        moveCellsToTheirInitialPosition()

        view.updateBattleView()
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
        view.setRing(ring)
        val step = ring.size / cells.size
        cells.forEachIndexed { index, cell -> cell.data.origin = ring[index * step] }
    }

    override fun doFullStep() {
        do {
            doPartialStep()
        } while (battleRoundSteps.peek() != firstStep)
    }

    override fun doPartialStep() {
        // Get next action
        val action = battleRoundSteps.poll()
        action.invoke()
        battleRoundSteps.add(action)
        view.updateBattleView()
    }

    private fun calculateBattleState() {
        battleState.directions.clear()
        battleState.rads.clear()

        // Each cell will move to the nearest hex within all enemies
        cells.forEachIndexed { i, cell ->
            var nearest: Hex = cell.data.origin
            var minDistance = Int.MAX_VALUE
            // Search for nearest enemy hex
            cells.forEachIndexed { j, enemy ->
                if (j != i) {
                    enemy.data.hexes.forEach {
                        val candidate = hexMath.add(it.value, enemy.data.origin)
                        val distance = hexMath.distance(cell.data.origin, candidate)
                        if (distance < minDistance) {
                            minDistance = distance
                            nearest = candidate
                        }
                    }
                }
            }
            // Find direction to move
            // There is a case when origin has same position as nearest enemy hex (origin is empty).
            // In such case current cell will choose east as moving direction. If both cells will choose
            // same direction they will move eternally. We should avoid such case, therefore we will choose random
            // direction for cell in such case.
            if (minDistance == 0) {
                battleState.directions.add((0..5).shuffled().last())
                battleState.rads.add(0f)
            } else {
                // Origin point
                val op = hexMath.hexToPixel(Layout.DUMMY, cell.data.origin)
                // Nearest hex point
                val hp = hexMath.hexToPixel(Layout.DUMMY, nearest)
                // Angle direction to enemy hex
                val angle = atan2(hp.y.toFloat() - op.y.toFloat(), hp.x.toFloat() - op.x.toFloat())
                battleState.rads.add(angle)
                // Determine direction based on angle
                battleState.directions.add(hexMath.radToNeighborDirection(angle))
            }
        }
    }

    private fun applyCellLogic(index: Int, cell: Cell) {
        correctBattleStateForCell(index)
        cell.applyCellLogic(battleState)
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
                hex.power -= hex.receivedDamage
                hex.receivedDamage = 0
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
            cell.data.hexes.forEach { (key, hex) ->
                // If power becomes less or equal then zero, hex should be removed from cell
                if (hex.power <= 0) {
                    hexesToRemove.add(key)
                }
            }
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

    //region Partial round steps
    private lateinit var firstStep: () -> Unit

    private val calculateBattleState = {
        calculateBattleState()
    }

    private val applyCellsLogic = {
        cells.forEachIndexed { index, cell -> applyCellLogic(index, cell) }
    }

    private val moveCells = {
        moveCells()
    }

    private val calculateDamages = {
        checkIntersections()
        checkNeighboring()
    }

    private val checkWhetherBattleEnds = {
        if (cells.size <= 1) {
            view.reportBattleEnd()
        }
    }
    //endregion
}