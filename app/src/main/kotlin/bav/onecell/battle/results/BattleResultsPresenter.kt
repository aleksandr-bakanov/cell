package bav.onecell.battle.results

import android.util.Log
import bav.onecell.common.Common
import bav.onecell.common.router.Router
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell

class BattleResultsPresenter(
        private val view: BattleResults.View,
        private val router: Router,
        private val cellRepo: RepositoryContract.CellRepo,
        private val resourceProvider: Common.ResourceProvider,
        private val dealtDamage: MutableMap<Int, Int> = mutableMapOf(),
        private val deadOrAliveCells: MutableMap<Int, Boolean> = mutableMapOf(),
        private val cellIndexes: MutableMap<Int, MutableList<Int>> = mutableMapOf()) : BattleResults.Presenter {

    override fun initialize(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>) {
        dealtDamage.keys.groupBy { cellRepo.getCell(it)?.data?.groupId }.forEach { entry ->
            entry.key?.let { groupId ->
                val indexes = mutableListOf<Int>()
                indexes.addAll(entry.value.sorted())
                cellIndexes[groupId] = indexes
            }
        }
        this.dealtDamage.putAll(dealtDamage)
        this.deadOrAliveCells.putAll(deadOrAliveCells)
        // TODO: convert dealt damage to money for each cell
    }

    override fun cellsCount(groupId: Int): Int = deadOrAliveCells.size

    override fun getCell(groupId: Int, position: Int): Cell? = cellRepo.getCell(cellIndexes[groupId]?.get(position) ?: -1)

    override fun getDealtDamage(groupId: Int, position: Int): Int = dealtDamage[cellIndexes[groupId]?.get(position) ?: -1] ?: 0

    override fun getDeadOrAlive(groupId: Int, position: Int): Boolean = deadOrAliveCells[cellIndexes[groupId]?.get(position) ?: -1] ?: false

    override fun goToHeroesScreen() {
        router.goToMain()
    }

    override fun getCellName(resourceId: String): String = resourceProvider.getString(resourceId) ?: ""

    companion object {
        private const val TAG = "BattleResultsPresenter"
    }
}
