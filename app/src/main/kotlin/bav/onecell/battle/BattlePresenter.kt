package bav.onecell.battle

import android.util.Log
import bav.onecell.common.router.Router
import bav.onecell.model.Cell
import bav.onecell.model.RepositoryContract

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

        for (c in cells) {
            Log.d(TAG, "cell.size = ${c.size()}")
        }

        battleFieldSize = cells.map { it.size() }.sum() * 2
        Log.d(TAG, "battleFieldSize = $battleFieldSize")
        view.setBackgroundFieldRadius(battleFieldSize)

        moveCellsToTheirInitialPosition()
    }

    private fun moveCellsToTheirInitialPosition() {

    }

    override fun doNextStep() {
        cells.forEach { applyCellLogic(it) }
        moveCells()
    }

    private fun applyCellLogic(cell: Cell) {

    }

    private fun moveCells() {

    }
}