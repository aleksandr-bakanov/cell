package bav.onecell.cellslist.cellselection

import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import kotlinx.android.synthetic.main.item_row_cell_for_selection.view.checkboxSelect
import kotlinx.android.synthetic.main.item_row_cell_for_selection.view.title

class CellForBattleRecyclerViewAdapter(private val presenter: CellsForBattle.Presenter) :
        RecyclerView.Adapter<CellForBattleRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell_for_selection, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount() = presenter.cellsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setCellTitle(presenter.getCell(position)?.data?.name)
    }

    class ViewHolder(val view: View, presenter: CellsForBattle.Presenter) : RecyclerView.ViewHolder(view) {
        init {
            view.checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                presenter.cellSelected(adapterPosition, isChecked)
            }
        }
        fun setCellTitle(title: String?) {
            view.title.text = title ?: ""
        }
    }
}
