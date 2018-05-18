package bav.onecell.cellslist

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.OneCellApplication
import bav.onecell.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_cell_list.buttonCreateNewCell
import kotlinx.android.synthetic.main.fragment_cell_list.recyclerViewCellList
import javax.inject.Inject

class CellsListFragment : Fragment(), CellsList.View {

    @Inject
    lateinit var presenter: CellsList.Presenter
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "$this onCreateView")
        return inflater.inflate(R.layout.fragment_cell_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "$this onActivityCreated")
        inject()

        // TODO: remove listeners when they aren't needed anymore
        buttonCreateNewCell.setOnClickListener { presenter.createNewCell() }

        recyclerViewCellList.layoutManager = LinearLayoutManager(context)
        recyclerViewCellList.adapter = CellRecyclerViewAdapter(presenter)

        disposables.add(presenter.cellRepoUpdateNotifier().subscribe {
            recyclerViewCellList.adapter.notifyDataSetChanged()
        })
        presenter.initialize()
    }

    override fun onPause() {
        Log.d(TAG, "$this onPause")
        presenter.onPause()
        super.onPause()
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

    companion object {
        private const val TAG = "CellsListFragment"
        @JvmStatic
        fun newInstance() = CellsListFragment()
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
