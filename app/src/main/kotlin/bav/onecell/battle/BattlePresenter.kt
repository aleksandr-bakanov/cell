package bav.onecell.battle

import bav.onecell.common.router.Router
import bav.onecell.model.Cell
import bav.onecell.model.RepositoryContract

class BattlePresenter(
        private val view: Battle.View,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : Battle.Presenter {

    private val cells = mutableListOf<Cell>()

    override fun initialize(cellIndexes: List<Int>) {
        for (i in cellIndexes) cellRepository.getCell(i)?.let { cells.add(it) }
        view.setBackgroundFieldRadius(10)
    }

    override fun doNextStep() {

    }
}