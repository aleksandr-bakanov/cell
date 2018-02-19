package bav.onecell.main

import bav.onecell.common.router.Router
import bav.onecell.model.Cell
import bav.onecell.model.CellRepository

class MainPresenter(
        private val view: Main.View,
        private val cellRepository: CellRepository,
        private val router: Router) : Main.Presenter {

    companion object {
        private const val TAG = "MainPresenter"
    }

    //region Overriden methods
    override fun createNewCell() {
        val cell = Cell()
        cellRepository.cells.add(cell)
        view.notifyCellRepoListUpdated()
    }

    override fun cellsCount(): Int = cellRepository.cells.size

    override fun openCellConstructor(cellIndex: Int) {
        router.goToCellConstructor(cellIndex)
    }
    //endregion
}