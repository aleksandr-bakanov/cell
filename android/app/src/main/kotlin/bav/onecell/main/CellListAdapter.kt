package bav.onecell.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import bav.onecell.model.CellRepository
import kotlinx.android.synthetic.main.item_row_cell.view.*

class CellListAdapter(private val cellRepository: CellRepository): RecyclerView.Adapter<CellListAdapter.CellViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_row_cell, parent, false)
        return CellViewHolder(view)
    }

    override fun getItemCount() = cellRepository.cells.size

    override fun onBindViewHolder(holder: CellViewHolder?, position: Int) {
        holder?.setCellTitle("Cell #$position")
    }

    class CellViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        fun setCellTitle(title: String) {
            view.title.text = title
        }
    }
}