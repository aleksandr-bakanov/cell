package bav.onecell.cellslist

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import bav.onecell.R
import kotlinx.android.synthetic.main.item_row_cell.view.buttonEditCell
import kotlinx.android.synthetic.main.item_row_cell.view.buttonEditCellRules
import kotlinx.android.synthetic.main.item_row_cell.view.buttonRemoveCell
import kotlinx.android.synthetic.main.item_row_cell.view.title

class CellRecyclerViewAdapter(private val presenter: CellsList.Presenter) :
        RecyclerView.Adapter<CellRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount() = presenter.cellsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setCellTitle(presenter.getCellName(position))
        holder.view.title.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { if (it.isNotEmpty()) presenter.setCellName(position) }
            }
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
    }

    class ViewHolder(val view: View, private val presenter: CellsList.Presenter) : RecyclerView.ViewHolder(view) {
        init {
            view.buttonEditCell.setOnClickListener {
                presenter.openCellEditor(adapterPosition)
            }
            view.buttonEditCellRules.setOnClickListener {
                presenter.openCellRulesEditor(adapterPosition)
            }
            view.buttonRemoveCell.setOnClickListener {
                presenter.removeCell(adapterPosition)
            }
        }

        fun setCellTitle(title: String) {
            view.title.setText(title, TextView.BufferType.EDITABLE)
        }
    }
}
