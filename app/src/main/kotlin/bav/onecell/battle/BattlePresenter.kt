package bav.onecell.battle

import bav.onecell.common.router.Router
import bav.onecell.model.Cell
import bav.onecell.model.RepositoryContract
import bav.onecell.model.Rules
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import bav.onecell.model.hexes.Layout
import kotlin.math.atan2
import kotlin.math.round

class BattlePresenter(
        private val view: Battle.View,
        private val hexMath: HexMath,
        private val rules: Rules,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : Battle.Presenter {

    companion object {
        private const val TAG = "BattlePresenter"
    }

    private val cells = mutableListOf<Cell>()
    private var battleFieldSize: Int = 0

    override fun initialize(cellIndexes: List<Int>) {
        // Make copy of cells
        for (i in cellIndexes) cellRepository.getCell(i)?.let { cells.add(it.clone()) }

        battleFieldSize = round(cells.map { it.size() }.sum() * 1.5).toInt()

        view.setBackgroundFieldRadius(battleFieldSize)
        view.setCells(cells)

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
        cells.forEachIndexed { index, cell -> cell.origin = ring[index * step] }
    }

    override fun doNextStep() {
        // Each cell applies its logic. TODO pass battle field state to cell's logic mechanism
        cells.forEach { applyCellLogic(it) }
        // Move cells according to rules
        moveCells()
        // Calculate damages
        calculateDamages()
        // Update view
        view.updateBattleView()
        // Check whether battle is ended
        checkWhetherBattleEnds()
    }

    private fun calculateDamages() {
        checkIntersections()
        checkNeighboring()
    }

    private fun applyCellLogic(cell: Cell) {

    }

    private fun moveCells() {
        // Each cell will move to the nearest hex within all enemies
        val directions = mutableListOf<Int>()
        cells.forEachIndexed { i, cell ->
            var nearest: Hex = cell.origin
            var minDistance = Int.MAX_VALUE
            // Search for nearest enemy hex
            cells.forEachIndexed { j, enemy ->
                if (j != i) {
                    enemy.hexes.forEach {
                        val candidate = hexMath.add(it.value, enemy.origin)
                        val distance = hexMath.distance(cell.origin, candidate)
                        if (distance < minDistance) {
                            minDistance = distance
                            nearest = candidate
                        }
                    }
                }
            }
            // Find direction to move
            // Origin point
            val op = hexMath.hexToPixel(Layout.DUMMY, cell.origin)
            // Nearest hex point
            val hp = hexMath.hexToPixel(Layout.DUMMY, nearest)
            // Angle direction to enemy hex
            val angle = atan2(hp.y.toFloat() - op.y.toFloat(), hp.x.toFloat() - op.x.toFloat())
            // Determine direction based on angle
            directions.add(hexMath.radToDir(angle))
        }
        // Move cells
        directions.forEachIndexed { index, direction ->
            cells[index].origin = hexMath.add(cells[index].origin, hexMath.getHexByDirection(direction))
        }
    }

    private fun checkIntersections() {
        // Get intersection of all cells
        val allHexes = mutableSetOf<Hex>()
        val intersectedHexes = mutableSetOf<Hex>()
        cells.forEach { cell ->
            cell.hexes.forEach { entry ->
                val hexInGlobalCoords = hexMath.add(cell.origin, entry.value)
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
                cell.hexes[hexMath.subtract(intersected, cell.origin).hashCode()]?.let {
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
                cell.hexes[hexMath.subtract(intersected, cell.origin).hashCode()]?.let {
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
            val cellOutline = cell.getOutlineHexes().map { hexMath.add(cell.origin, it) }
            // Get all enemy hexes
            val enemyHexes = mutableSetOf<Hex>()
            cells.filter { it != cell }.forEach { enemy ->
                enemyHexes.addAll(enemy.hexes.values.map { hexMath.add(enemy.origin, it).withPower(it.power) })
            }
            // Get all enemy hexes which are neighbors to us
            val neighboringHexes = enemyHexes.intersect(cellOutline)
            // Come through all our hexes
            cell.hexes.values.forEach { hex ->
                val hexInGlobal = hexMath.add(hex, cell.origin)
                val neighbors = neighboringHexes.intersect(hexMath.hexNeighbors(hexInGlobal))
                // Saving damage to our hex
                neighbors.forEach { hex.receivedDamage += it.power }
            }
        }
        // Deal real damage
        cells.forEach { cell ->
            cell.hexes.values.forEach { hex ->
                hex.power -= hex.receivedDamage
                hex.receivedDamage = 0
            }
        }
        checkCellsVitality()
    }

    private fun checkCellsVitality() {
        val cellsToRemove = mutableListOf<Int>()
        cells.forEachIndexed { index, cell ->
            // Remove powerless hexes
            val hexesToRemove = mutableListOf<Int>()
            cell.hexes.forEach { key, hex ->
                // If power becomes less or equal then zero, hex should be removed from cell
                if (hex.power <= 0) {
                    hexesToRemove.add(key)
                }
            }
            hexesToRemove.forEach { cell.hexes.remove(it) }
            // If connectivity of life and energy hexes has been broken then cell dies
            if (!rules.checkHexesConnectivity(cell.hexes.values.filter {
                        it.type == Hex.Type.LIFE || it.type == Hex.Type.ENERGY
                    })) cellsToRemove.add(index)
        }
        cellsToRemove.sortDescending()
        cellsToRemove.forEach { cells.removeAt(it) }
    }

    private fun checkWhetherBattleEnds() {
        if (cells.size <= 1) {
            view.reportBattleEnd()
        }
    }
}