package bav.onecell.battle.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.extensions.visible
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.hexes.Hex
import kotlinx.android.synthetic.main.fragment_battle_results.buttonToHeroesScreen
import kotlinx.android.synthetic.main.fragment_battle_results.buttonTryAgain
import kotlinx.android.synthetic.main.fragment_battle_results.recyclerViewBattleResultsEnemies
import kotlinx.android.synthetic.main.fragment_battle_results.recyclerViewBattleResultsFriends
import org.json.JSONObject
import javax.inject.Inject

class BattleResultsFragment: androidx.fragment.app.Fragment(), BattleResults.View {

    @Inject lateinit var presenter: BattleResults.Presenter
    @Inject lateinit var drawUtils: DrawUtils
    @Inject lateinit var resourceProvider: Common.ResourceProvider
    @Inject lateinit var gameState: Common.GameState

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_battle_results, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        initializePresenter(arguments)
        initializeButtons(arguments)

        recyclerViewBattleResultsFriends.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerViewBattleResultsFriends.adapter = BattleResultsRecyclerViewAdapter(presenter, drawUtils)
        recyclerViewBattleResultsEnemies.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerViewBattleResultsEnemies.adapter = BattleResultsRecyclerViewAdapter(presenter, drawUtils, presenter.getEnemiesGroupId())
    }
    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(BattleResultsModule(this))
                .inject(this)
    }

    private fun initializePresenter(arguments: Bundle?) {
        arguments?.let {
            val cellIndexes = it.getIntArray(CELL_INDEXES)
            val dealtDamage = it.getIntArray(DEALT_DAMAGE)
            val deadOrAlive = it.getBooleanArray(DEAD_OR_ALIVE)
            val dd = mutableMapOf<Int, Int>()
            val doa = mutableMapOf<Int, Boolean>()
            cellIndexes?.forEachIndexed { i, id ->
                dd[id] = dealtDamage[i]
                doa[id] = deadOrAlive[i]
            }
            presenter.initialize(dd, doa)
        }
    }

    private fun initializeButtons(arguments: Bundle?) {
        arguments?.let {
            val nextScene = resourceProvider.getIdIdentifier(it.getString(Consts.NEXT_SCENE))
            val prevScene = resourceProvider.getIdIdentifier(it.getString(PREVIOUS_SCENE))
            val isBattleWon = it.getBoolean(IS_BATTLE_WON)
            buttonToHeroesScreen.visible = isBattleWon
            buttonTryAgain.visible = !isBattleWon
            if (isBattleWon) {
                buttonToHeroesScreen.setOnClickListener { view ->
                    view.findNavController().navigate(nextScene)
                }
                rewardForBattle(it.getString(Consts.BATTLE_REWARD, ""))
            }
            else {
                buttonTryAgain.setOnClickListener { view ->
                    view.findNavController().navigate(prevScene)
                }
            }
        }
    }

    private fun rewardForBattle(rewardJson: String) {
        val reward = JSONObject(rewardJson)
        for (index in arrayOf(Consts.KITTARO_INDEX, Consts.ZOI_INDEX, Consts.AIMA_INDEX)) {
            val hexReward = reward.optJSONObject(index.toString())
            if (hexReward != null) {
                presenter.getCell(index)?.let { cell ->
                    val bucket = cell.data.hexBucket
                    for (type in Hex.Type.values().filter { it != Hex.Type.REMOVE })
                        bucket[type.ordinal] = bucket.getOrElse(type.ordinal, Consts.ZERO) + hexReward.optInt(
                                type.toString())
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
    //endregion

    companion object {
        private const val TAG = "BattleResultsFragment"

        const val DEALT_DAMAGE = "dealt_damage"
        const val DEAD_OR_ALIVE = "dead_or_alive"
        const val CELL_INDEXES = "cell_indexes"
        const val IS_BATTLE_WON = "is_battle_won"
        private const val PREVIOUS_SCENE = "previous_scene"
        const val GAME_STATE_CHANGES = "gameStateChanges"

        fun newInstance(bundle: Bundle?): BattleResultsFragment {
            val fragment = BattleResultsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
