package bav.onecell.battle

import android.util.Log
import bav.onecell.common.router.Router
import bav.onecell.model.Cell
import bav.onecell.model.RepositoryContract
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Layout
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.round

class BattlePresenter(
        private val view: Battle.View,
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
        var hex = Hex.hexMultiply(Hex.hexDirection(4), ringRadius)
        for (i in 0..5) {
            for (j in 0..(ringRadius - 1)) {
                ring.add(hex)
                hex = Hex.hexNeighbor(hex, i)
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
                        val candidate = Hex.hexAdd(it, enemy.origin)
                        val distance = Hex.hexDistance(cell.origin, candidate)
                        if (distance < minDistance) {
                            minDistance = distance
                            nearest = candidate
                        }
                    }
                }
            }
            // Find direction to move
            // Origin point
            val op = Hex.hexToPixel(Layout.DUMMY, cell.origin)
            // Nearest hex point
            val hp = Hex.hexToPixel(Layout.DUMMY, nearest)
            // Angle direction to enemy hex
            val angle = atan2(hp.y.toFloat() - op.y.toFloat(), hp.x.toFloat() - op.x.toFloat())
            // Determine direction based on angle
            directions.add(Hex.radToDir(angle))
        }
        // Move cells
        directions.forEachIndexed { index, direction ->
            cells[index].origin = Hex.hexAdd(cells[index].origin, Hex.hexDirection(direction))
        }
    }

    private fun checkIntersections() {

    }

    private fun checkNeighboring() {

    }
}