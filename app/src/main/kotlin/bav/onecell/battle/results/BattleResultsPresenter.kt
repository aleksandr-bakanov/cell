package bav.onecell.battle.results

import bav.onecell.common.router.Router
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell

class BattleResultsPresenter(
        private val view: BattleResults.View,
        private val router: Router,
        private val cellRepo: RepositoryContract.CellRepo) : BattleResults.Presenter {

    override fun initialize(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>) {

    }

    override fun cellsCount(): Int {
        return 0
    }

    override fun getCell(index: Int): Cell? = cellRepo.getCell(index)
}
