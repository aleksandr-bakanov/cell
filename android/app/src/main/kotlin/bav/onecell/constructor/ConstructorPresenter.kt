package bav.onecell.constructor

import bav.onecell.common.router.Router
import bav.onecell.model.CellRepository

class ConstructorPresenter(
        private val view: Constructor.View,
        private val cellRepository: CellRepository,
        private val router: Router) : Constructor.Presenter {

    private var cellIndex: Int = 0

    override fun initialize(cellIndex: Int) {
        this.cellIndex = cellIndex
    }
}