package bav.onecell.cellslist.cellselection

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.cellslist.CellsListModule
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_cell_list.recyclerViewCellList
import kotlinx.android.synthetic.main.fragment_choose_cells_for_battle.buttonStartBattle
import kotlinx.android.synthetic.main.item_row_cell_for_selection.view.checkboxSelect
import javax.inject.Inject

class CellsForBattleFragment : Fragment(), CellsForBattle.View {

    @Inject
    lateinit var presenter: CellsForBattle.Presenter
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "$this onCreateView")
        return inflater.inflate(R.layout.fragment_choose_cells_for_battle, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "$this onActivityCreated")
        inject()

        buttonStartBattle.setOnClickListener { openBattleView() }

        recyclerViewCellList.layoutManager = LinearLayoutManager(context)
        recyclerViewCellList.adapter = CellForBattleRecyclerViewAdapter(presenter)

        disposables.add(presenter.cellRepoUpdateNotifier().subscribe {
            recyclerViewCellList.adapter.notifyDataSetChanged()
        })
        presenter.initialize()
    }

    override fun onDestroyView() {
        Log.d(TAG, "$this onDestroyView")
        disposables.dispose()
        super.onDestroyView()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(CellsListModule())
                .inject(this)
    }

    private fun openBattleView() {
        val indexes = mutableListOf<Int>()
        for (i in 0 until recyclerViewCellList.childCount) {
            val viewHolder = recyclerViewCellList
                    .findViewHolderForAdapterPosition(i) as? CellForBattleRecyclerViewAdapter.ViewHolder
            viewHolder?.let {
                if (it.view.checkboxSelect.isChecked) indexes.add(i)
            }
        }
        if (indexes.size >= 2) presenter.startBattle(indexes)
        // TODO: move string to resources
        else Toast.makeText(activity, "Select at least two cells", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "CellsForBattleFragment"
        @JvmStatic
        fun newInstance() = CellsForBattleFragment()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, "$this onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "$this onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "$this onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "$this onResume")
    }

    override fun onPause() {
        Log.d(TAG, "$this onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "$this onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "$this onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG, "$this onDetach")
        super.onDetach()
    }
}
