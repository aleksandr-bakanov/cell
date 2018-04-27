package bav.onecell.main

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import bav.onecell.R
import kotlinx.android.synthetic.main.fragment_cell_list.buttonCreateNewCell
import kotlinx.android.synthetic.main.fragment_cell_list.buttonStartBattle
import kotlinx.android.synthetic.main.fragment_cell_list.recyclerViewCellList
import kotlinx.android.synthetic.main.item_row_cell.view.checkboxSelect

class CellListFragment : Fragment() {

    private lateinit var listener: OnListFragmentInteractionListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cell_list, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonCreateNewCell.setOnClickListener { listener.providePresenter().createNewCell() }
        buttonStartBattle.setOnClickListener { openBattleView() }

        recyclerViewCellList.layoutManager = LinearLayoutManager(context)
        recyclerViewCellList.adapter = CellRecyclerViewAdapter(listener.providePresenter(), listener)
    }

    private fun openBattleView() {
        val indexes = mutableListOf<Int>()
        for (i in 0 until recyclerViewCellList.childCount) {
            val viewHolder = recyclerViewCellList.findViewHolderForAdapterPosition(i) as? CellRecyclerViewAdapter.CellViewHolder
            viewHolder?.let {
                if (it.view.checkboxSelect.isChecked) indexes.add(i)
            }
        }
        if (indexes.size >= 2) listener.providePresenter().openBattleView(indexes)
        else Toast.makeText(context, "Select at least two cells", Toast.LENGTH_LONG).show()
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        fun providePresenter(): Main.Presenter
    }

    companion object {
        @JvmStatic
        fun newInstance() = CellListFragment()
    }
}
