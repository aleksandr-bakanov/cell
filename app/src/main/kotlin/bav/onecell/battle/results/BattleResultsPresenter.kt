package bav.onecell.battle.results

import android.util.Log
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.router.Router
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell

class BattleResultsPresenter(
        private val view: BattleResults.View,
        private val router: Router,
        private val cellRepo: RepositoryContract.CellRepo,
        private val resourceProvider: Common.ResourceProvider,
        private val results: MutableList<CellResults> = mutableListOf()) : BattleResults.Presenter {

    override fun initialize(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>) {
        dealtDamage.keys.forEach { index ->
            results.add(CellResults(index, deadOrAliveCells[index] ?: false, dealtDamage[index] ?: 0))
        }
    }

    override fun cellsCount(): Int = results.size

    override fun getCell(position: Int): Cell? = cellRepo.getCell(results[position].index)

    override fun getDealtDamage(position: Int): Int = results[position].dealtDamage

    override fun getDeadOrAlive(position: Int): Boolean = results[position].isAlive

    override fun goToHeroesScreen() {
        router.goToMain()
    }

    override fun getCellName(resourceId: String): String = resourceProvider.getString(resourceId) ?: ""

    data class CellResults(val index: Int, val isAlive: Boolean, val dealtDamage: Int)

    companion object {
        private const val TAG = "BattleResultsPresenter"
    }
}
