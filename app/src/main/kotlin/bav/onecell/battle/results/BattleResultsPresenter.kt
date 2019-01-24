package bav.onecell.battle.results

import android.util.Log
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.Consts.Companion.GAME_STATE_CHANGES
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import org.json.JSONObject

class BattleResultsPresenter(
        private val view: BattleResults.View,
        private val cellRepo: RepositoryContract.CellRepo,
        private val resourceProvider: Common.ResourceProvider,
        private val gameState: Common.GameState,
        private val results: MutableMap<Int, List<CellResults>> = mutableMapOf(),
        private var groupIndexes: List<Int>? = null,
        private val rewards: MutableMap<Int /*cell index*/, MutableMap<Int /*hex type*/, Int /*count*/>> = mutableMapOf())
    : BattleResults.Presenter {

    override fun initialize(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>, rewardJson: String) {
        val cellsResults = mutableListOf<CellResults>()
        dealtDamage.keys.forEach { index ->
            cellsResults.add(CellResults(index, deadOrAliveCells[index] ?: false, dealtDamage[index] ?: 0))
        }
        cellsResults.groupBy { cellRepo.getCell(it.index)?.data?.groupId }.forEach { entry ->
            entry.key?.let { groupId -> results[groupId] = entry.value.sortedBy { it.index } }
        }
        groupIndexes = results.keys.sorted()
        rewardForBattle(rewardJson)
    }

    override fun cellsCount(groupId: Int): Int = results[groupId]?.size ?: 0

    override fun getCell(index: Int): Cell? = cellRepo.getCell(index)

    override fun getCell(groupId: Int, position: Int): Cell? = cellRepo.getCell(results[groupId]?.get(position)?.index ?: -1)

    override fun getDealtDamage(groupId: Int, position: Int): Int = results[groupId]?.get(position)?.dealtDamage ?: 0

    override fun getDeadOrAlive(groupId: Int, position: Int): Boolean = results[groupId]?.get(position)?.isAlive ?: false

    override fun groupsCount(): Int = results.size

    override fun getGroupId(position: Int): Int = groupIndexes?.get(position) ?: -1

    override fun getCellName(resourceId: String): String = resourceProvider.getString(resourceId) ?: ""

    override fun getRewardByType(groupId: Int, position: Int, type: Int): Int {
        val cellIndex = results[groupId]?.get(position)?.index ?: -1
        return rewards[cellIndex]?.get(type) ?: 0
    }

    private fun rewardForBattle(rewardJson: String) {
        val reward = JSONObject(rewardJson)
        for (index in arrayOf(Consts.KITTARO_INDEX, Consts.ZOI_INDEX, Consts.AIMA_INDEX)) {
            val hexReward = reward.optJSONObject(index.toString())
            if (hexReward != null) {
                getCell(index)?.let { cell ->
                    val cellReward = mutableMapOf<Int, Int>()
                    val bucket = cell.data.hexBucket
                    for (type in Hex.Type.values().filter { it != Hex.Type.REMOVE }) {
                        val hexes = hexReward.optInt(type.toString())
                        if (hexes > 0) {
                            bucket[type.ordinal] = bucket.getOrElse(type.ordinal, Consts.ZERO) + hexes
                            cellReward[type.ordinal] = hexes
                        }
                    }
                    rewards[index] = cellReward
                }
            }
        }
        reward.optJSONObject(GAME_STATE_CHANGES)?.let { gameStateChanges ->
            // Changes should contain booleans
            for (decision in gameStateChanges.keys()) {
                gameState.setDecision(decision, gameStateChanges.getBoolean(decision))
            }
        }
    }

    data class CellResults(val index: Int, val isAlive: Boolean, val dealtDamage: Int)

    companion object {
        private const val TAG = "BattleResultsPresenter"
    }
}
