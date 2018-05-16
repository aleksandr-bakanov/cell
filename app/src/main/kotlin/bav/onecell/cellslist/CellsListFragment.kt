package bav.onecell.cellslist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
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
        return inflater.inflate(R.layout.fragment_cell_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
        presenter.onPause()
        super.onPause()
    }

    override fun openCellEditorView(cellIndex: Int) {

    }

    override fun openCellLogicEditorView(cellIndex: Int) {

    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(CellsListModule(this))
                .inject(this)
    }

    companion object {
        @JvmStatic
        fun newInstance() = CellsListFragment()
    }
}
