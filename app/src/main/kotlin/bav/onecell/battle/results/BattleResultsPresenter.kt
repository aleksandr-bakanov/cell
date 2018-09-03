package bav.onecell.battle.results

import bav.onecell.common.router.Router
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell

class BattleResultsPresenter(
        private val view: BattleResults.View,
        private val router: Router,
        private val cellRepo: RepositoryContract.CellRepo,
        private val dealtDamage: MutableMap<Int, Int> = mutableMapOf(),
        private val deadOrAliveCells: MutableMap<Int, Boolean> = mutableMapOf(),
        private val cellIndexes: MutableList<Int> = mutableListOf()) : BattleResults.Presenter {

    override fun initialize(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>) {
        cellIndexes.addAll(dealtDamage.keys.sorted())
        this.dealtDamage.putAll(dealtDamage)
        this.deadOrAliveCells.putAll(deadOrAliveCells)
    }

    override fun cellsCount(): Int = deadOrAliveCells.size

    override fun getCell(position: Int): Cell? = cellRepo.getCell(cellIndexes[position])

    override fun getDealtDamage(position: Int): Int = dealtDamage[cellIndexes[position]] ?: 0

    override fun getDeadOrAlive(position: Int): Boolean = deadOrAliveCells[cellIndexes[position]] ?: false

    override fun goToHeroesScreen() {
        router.goToMain()
    }
}
