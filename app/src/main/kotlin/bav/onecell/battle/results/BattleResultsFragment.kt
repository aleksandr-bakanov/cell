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
import bav.onecell.common.view.DrawUtils
import kotlinx.android.synthetic.main.fragment_battle_results.buttonToHeroesScreen
import kotlinx.android.synthetic.main.fragment_battle_results.recyclerViewBattleResults
import javax.inject.Inject

class BattleResultsFragment: androidx.fragment.app.Fragment(), BattleResults.View {

    @Inject lateinit var presenter: BattleResults.Presenter
    @Inject lateinit var drawUtils: DrawUtils
    @Inject lateinit var resourceProvider: Common.ResourceProvider

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_battle_results, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()
        recyclerViewBattleResults.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerViewBattleResults.adapter = BattleResultsRecyclerViewAdapter(presenter, drawUtils)
        initializePresenter(arguments)
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
            val nextScene = resourceProvider.getIdIdentifier(it.getString(Consts.NEXT_SCENE))
            val dd = mutableMapOf<Int, Int>()
            val doa = mutableMapOf<Int, Boolean>()
            cellIndexes?.forEachIndexed { i, id ->
                dd[id] = dealtDamage[i]
                doa[id] = deadOrAlive[i]
            }
            presenter.initialize(dd, doa)
            buttonToHeroesScreen.setOnClickListener { view ->
                view.findNavController().navigate(nextScene)
            }
        }
    }
    //endregion

    companion object {
        private const val TAG = "BattleResultsFragment"

        const val DEALT_DAMAGE = "dealt_damage"
        const val DEAD_OR_ALIVE = "dead_or_alive"
        const val CELL_INDEXES = "cell_indexes"

        fun newInstance(bundle: Bundle?): BattleResultsFragment {
            val fragment = BattleResultsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
