package bav.onecell.cellslist.cellselection

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import kotlinx.android.synthetic.main.item_row_cell.view.title

class CellForBattleRecyclerViewAdapter(private val presenter: CellsForBattle.Presenter) :
        RecyclerView.Adapter<CellForBattleRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell_for_selection, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount() = presenter.cellsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setCellTitle("Cell #$position")
    }

    class ViewHolder(val view: View, private val presenter: CellsForBattle.Presenter) : RecyclerView.ViewHolder(view) {
        fun setCellTitle(title: String) {
            view.title.text = title
        }
    }
}
