package bav.onecell.constructor

import bav.onecell.common.router.Router
import bav.onecell.model.Cell
import bav.onecell.model.RepositoryContract

class ConstructorPresenter(
        private val view: Constructor.View,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : Constructor.Presenter {

    private var cell: Cell? = null

    override fun initialize(cellIndex: Int) {
        cell = cellRepository.getCell(cellIndex)
        view.setBackgroundFieldRadius(3)
        view.setCell(cell)
    }
}