package bav.onecell.main

import bav.onecell.common.router.Router
import bav.onecell.model.Cell
import bav.onecell.model.RepositoryContract

class MainPresenter(
        private val view: Main.View,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : Main.Presenter {

    companion object {
        private const val TAG = "MainPresenter"
    }

    //region Overriden methods
    override fun createNewCell() {
        val cell = Cell()
        cellRepository.addCell(cell)
        view.notifyCellRepoListUpdated()
    }

    override fun cellsCount(): Int = cellRepository.cellsCount()

    override fun openCellConstructor(cellIndex: Int) {
        router.goToCellConstructor(cellIndex)
    }
    //endregion
}