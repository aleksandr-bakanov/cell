package bav.onecell.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R

import kotlinx.android.synthetic.main.item_row_cell.view.buttonEditCell
import kotlinx.android.synthetic.main.item_row_cell.view.buttonEditCellRules
import kotlinx.android.synthetic.main.item_row_cell.view.buttonRemoveCell
import kotlinx.android.synthetic.main.item_row_cell.view.title

class CellRecyclerViewAdapter(private val presenter: Main.Presenter) :
        RecyclerView.Adapter<CellRecyclerViewAdapter.CellViewHolder>() {

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
            view.buttonEditCell.setOnClickListener { presenter.openCellEditor(index) }
            view.buttonEditCellRules.setOnClickListener { presenter.openCellRulesEditor(index) }
            view.buttonRemoveCell.setOnClickListener { presenter.removeCell(index) }
        }

        fun setCellTitle(title: String) {
            view.title.text = title
        }
    }
}
