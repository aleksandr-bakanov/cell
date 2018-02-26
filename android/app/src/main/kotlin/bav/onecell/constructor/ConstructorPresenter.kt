package bav.onecell.constructor

import bav.onecell.common.router.Router
import bav.onecell.model.Cell
import bav.onecell.model.CellRepository

class ConstructorPresenter(
        private val view: Constructor.View,
        private val cellRepository: CellRepository,
        private val router: Router) : Constructor.Presenter {

    private lateinit var cell: Cell


    override fun initialize(cellIndex: Int) {
        cell = cellRepository.cells[cellIndex]
    }
}