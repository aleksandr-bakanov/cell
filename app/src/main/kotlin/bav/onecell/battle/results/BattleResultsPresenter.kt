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
        private val results: MutableMap<Int, List<CellResults>> = mutableMapOf(),
        private var groupIndexes: List<Int>? = null) : BattleResults.Presenter {

    override fun initialize(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>) {
        val cellsResults = mutableListOf<CellResults>()
        dealtDamage.keys.forEach { index ->
            cellsResults.add(CellResults(index, deadOrAliveCells[index] ?: false, dealtDamage[index] ?: 0))
        }
        cellsResults.groupBy { cellRepo.getCell(it.index)?.data?.groupId }.forEach { entry ->
            entry.key?.let { groupId -> results[groupId] = entry.value.sortedBy { it.index } }
        }
        groupIndexes = results.keys.sorted()
    }

    override fun cellsCount(groupId: Int): Int = results[groupId]?.size ?: 0

    override fun getCell(index: Int): Cell? = cellRepo.getCell(index)

    override fun getCell(groupId: Int, position: Int): Cell? = cellRepo.getCell(results[groupId]?.get(position)?.index ?: -1)

    override fun getDealtDamage(groupId: Int, position: Int): Int = results[groupId]?.get(position)?.dealtDamage ?: 0

    override fun getDeadOrAlive(groupId: Int, position: Int): Boolean = results[groupId]?.get(position)?.isAlive ?: false

    override fun groupsCount(): Int = results.size

    override fun getGroupId(position: Int): Int = groupIndexes?.get(position) ?: -1

    override fun goToHeroesScreen() {
        router.goToMain()
    }

    override fun getCellName(resourceId: String): String = resourceProvider.getString(resourceId) ?: ""

    data class CellResults(val index: Int, val isAlive: Boolean, val dealtDamage: Int)

    companion object {
        private const val TAG = "BattleResultsPresenter"
    }
}
