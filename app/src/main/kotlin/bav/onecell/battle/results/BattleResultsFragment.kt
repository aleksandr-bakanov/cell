package bav.onecell.battle.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.hexes.Hex
import kotlinx.android.synthetic.main.fragment_battle_results.buttonToHeroesScreen
import kotlinx.android.synthetic.main.fragment_battle_results.buttonTryAgain
import kotlinx.android.synthetic.main.fragment_battle_results.recyclerViewBattleResults
import org.json.JSONObject
import javax.inject.Inject

class BattleResultsFragment: androidx.fragment.app.Fragment(), BattleResults.View {

    @Inject lateinit var presenter: BattleResults.Presenter
    @Inject lateinit var drawUtils: DrawUtils
    @Inject lateinit var resourceProvider: Common.ResourceProvider
    @Inject lateinit var gameState: Common.GameState
    @Inject lateinit var analytics: Common.Analytics

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_battle_results, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        initializePresenter(arguments)
        initializeButtons(arguments)

        recyclerViewBattleResults.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
        recyclerViewBattleResults.adapter = BattleResultsRecyclerViewAdapter(presenter, drawUtils, resourceProvider)
    }

    override fun onResume() {
        super.onResume()
        analytics.setCurrentScreen(requireActivity(), SCREEN_NAME, this::class.qualifiedName)
    }

    override fun onPause() {
        gameState.setLastNavDestinationId(findNavController().currentDestination?.id ?: 0)
        super.onPause()
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
            val isBattleWon = it.getBoolean(IS_BATTLE_WON)
            val sceneId = it.getString(Consts.SCENE_ID, "")
            var reward = "{}"
            if (!gameState.hasBattleBeenWon(sceneId) && isBattleWon) {
                reward = it.getString(Consts.BATTLE_REWARD, "{}")
                gameState.setBattleHasBeenWon(sceneId)
            }
            presenter.initialize(dd, doa, reward)
        }
    }

    private fun initializeButtons(arguments: Bundle?) {
        arguments?.let {
            val nextScene = resourceProvider.getIdIdentifier(getString(it.getInt(Consts.NEXT_SCENE)))
            val prevScene = resourceProvider.getIdIdentifier(getString(it.getInt(PREVIOUS_SCENE)))
            val isBattleWon = it.getBoolean(IS_BATTLE_WON)
            buttonToHeroesScreen.visibility = if (isBattleWon) View.VISIBLE else View.INVISIBLE
            buttonTryAgain.visibility = if (!isBattleWon) View.VISIBLE else View.INVISIBLE
            if (isBattleWon) {
                buttonToHeroesScreen.setOnClickListener { view ->
                    view.findNavController().navigate(nextScene)
                }
            }
            else {
                buttonTryAgain.setOnClickListener { view ->
                    view.findNavController().navigate(prevScene)
                }
            }
        }
    }
    //endregion

    companion object {
        private const val TAG = "BattleResultsFragment"
        private const val SCREEN_NAME = "Battle results"

        const val DEALT_DAMAGE = "dealt_damage"
        const val DEAD_OR_ALIVE = "dead_or_alive"
        const val CELL_INDEXES = "cell_indexes"
        const val IS_BATTLE_WON = "is_battle_won"
        private const val PREVIOUS_SCENE = "previous_scene"
    }
}
