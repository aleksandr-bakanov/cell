package bav.onecell.cellslist.cellselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.battle.BattleFragment
import bav.onecell.cellslist.CellsListModule
import bav.onecell.model.InitialBattleParams
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_choose_cells_for_battle.buttonStartBattle
import kotlinx.android.synthetic.main.fragment_choose_cells_for_battle.recyclerViewCellList
import kotlinx.android.synthetic.main.item_row_cell_for_selection.view.checkboxSelect
import javax.inject.Inject

class CellsForBattleFragment : Fragment(), CellsForBattle.View {

    @Inject
    lateinit var presenter: CellsForBattle.Presenter
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_cells_for_battle, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        buttonStartBattle.setOnClickListener { openBattleView(it) }

        recyclerViewCellList.layoutManager = LinearLayoutManager(context)
        recyclerViewCellList.adapter = CellForBattleRecyclerViewAdapter(presenter)

        disposables.add(presenter.cellRepoUpdateNotifier().subscribe {
            recyclerViewCellList.adapter?.notifyDataSetChanged()
        })
        presenter.initialize()
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(CellsListModule())
                .inject(this)
    }

    private fun openBattleView(view: View) {
        val indexes = mutableListOf<Int>()
        for (i in 0 until recyclerViewCellList.childCount) {
            val viewHolder = recyclerViewCellList
                    .findViewHolderForAdapterPosition(i) as? CellForBattleRecyclerViewAdapter.ViewHolder
            viewHolder?.let {
                if (it.view.checkboxSelect.isChecked) indexes.add(i)
            }
        }
        if (indexes.size >= 2) {
            val params = InitialBattleParams()
            params.cellIndexes.addAll(indexes)
            val bundle = bundleOf(BattleFragment.EXTRA_PARAMS to InitialBattleParams.toJson(params))
            view.findNavController().navigate(R.id.action_cellsForBattleFragment_to_battleFragment, bundle)
        }
        // TODO: move string to resources
        else Toast.makeText(activity, "Select at least two cells", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "CellsForBattleFragment"
        @JvmStatic
        fun newInstance() = CellsForBattleFragment()
    }
}
