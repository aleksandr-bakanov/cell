package bav.onecell.main

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import bav.onecell.OneCellApplication
import bav.onecell.R
import kotlinx.android.synthetic.main.activity_main.buttonCreateNewCell
import kotlinx.android.synthetic.main.activity_main.buttonStartBattle
import kotlinx.android.synthetic.main.activity_main.recyclerViewCellList
import kotlinx.android.synthetic.main.item_row_cell.view.buttonEditCell
import kotlinx.android.synthetic.main.item_row_cell.view.checkboxSelect
import kotlinx.android.synthetic.main.item_row_cell.view.title
import javax.inject.Inject

class MainActivity : Activity(), Main.View {

    @Inject
    lateinit var presenter: Main.Presenter

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inject()

        buttonCreateNewCell.setOnClickListener { presenter.createNewCell() }
        buttonStartBattle.setOnClickListener { openBattleView() }

        recyclerViewCellList.layoutManager = LinearLayoutManager(this)
        recyclerViewCellList.adapter = CellListAdapter(presenter)
    }
    //endregion

    //region Private methods
    private fun inject() {
        (application as OneCellApplication).appComponent
                .plus(MainModule(this))
                .inject(this)
    }

    private fun openBattleView() {
        val indexes = mutableListOf<Int>()
        for (i in 0..(recyclerViewCellList.childCount - 1)) {
            if ((recyclerViewCellList.findViewHolderForAdapterPosition(i) as CellListAdapter.CellViewHolder)
                    .view.checkboxSelect.isChecked) {
                indexes.add(i)
            }
        }
        if (indexes.size >= 2) presenter.openBattleView(indexes)
        else Toast.makeText(this, "Select at least two cells", Toast.LENGTH_LONG).show()
    }
    //endregion

    //region Overridden methods
    override fun notifyCellRepoListUpdated() {
        recyclerViewCellList.adapter.notifyDataSetChanged()
    }
    //endregion

    class CellListAdapter(private val presenter: Main.Presenter) :
            RecyclerView.Adapter<CellListAdapter.CellViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_row_cell, parent, false)
            return CellViewHolder(view, presenter)
        }

        override fun getItemCount() = presenter.cellsCount()

        override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
            holder.setCellTitle("Cell #$position")
            holder.index = position
        }

        class CellViewHolder(val view: View, private val presenter: Main.Presenter) :
                RecyclerView.ViewHolder(view) {

            var index: Int = 0

            init {
                view.buttonEditCell.setOnClickListener { presenter.openCellConstructor(index) }
            }

            fun setCellTitle(title: String) {
                view.title.text = title
            }
        }
    }
}