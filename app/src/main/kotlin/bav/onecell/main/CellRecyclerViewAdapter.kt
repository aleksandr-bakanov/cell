package bav.onecell.main

import android.graphics.Color
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
        RecyclerView.Adapter<CellRecyclerViewAdapter.ViewHolder>() {

    private var selectedItemPosition = RecyclerView.NO_POSITION
    private val notifyItemChanged = { index: Int, vh: ViewHolder ->
        if (vh.adapterPosition == RecyclerView.NO_POSITION) {
            // do nothing
        } else {
            notifyItemChanged(selectedItemPosition)
            notifyItemChanged(index)
            selectedItemPosition = index
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell, parent, false)
        return ViewHolder(view, presenter, notifyItemChanged)
    }

    override fun getItemCount() = presenter.cellsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setCellTitle("Cell #$position")
        holder.index = position
        holder.view.setBackgroundColor(if (selectedItemPosition == position) Color.GREEN else Color.TRANSPARENT)
    }

    class ViewHolder(val view: View, private val presenter: Main.Presenter,
                     private val notifyItemChanged: (Int, ViewHolder) -> Unit) : RecyclerView.ViewHolder(view) {
        // TODO: looks like index and adapterPosition are the same
        var index: Int = 0

        init {
            view.buttonEditCell.setOnClickListener {
                presenter.openCellEditor(index)
                notifyItemChanged(adapterPosition, this)
            }
            view.buttonEditCellRules.setOnClickListener {
                presenter.openCellRulesEditor(index)
                notifyItemChanged(adapterPosition, this)
            }
            view.buttonRemoveCell.setOnClickListener {
                presenter.removeCell(index)
                notifyItemChanged(adapterPosition, this)
            }
        }

        fun setCellTitle(title: String) {
            view.title.text = title
        }
    }
}
