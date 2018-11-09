package bav.onecell.cellslist.cellselection

import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import bav.onecell.R
import bav.onecell.common.Common
import kotlinx.android.synthetic.main.item_row_cell_for_selection.view.checkboxSelect
import kotlinx.android.synthetic.main.item_row_cell_for_selection.view.title

class CellForBattleRecyclerViewAdapter(private val presenter: CellsForBattle.Presenter,
                                       private val resourceProvider: Common.ResourceProvider) :
        RecyclerView.Adapter<CellForBattleRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell_for_selection, parent, false)
        return ViewHolder(view, presenter, resourceProvider)
    }

    override fun getItemCount() = presenter.cellsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.inflate(position)
    }

    class ViewHolder(val view: View, private val presenter: CellsForBattle.Presenter,
                     private val resourceProvider: Common.ResourceProvider) : RecyclerView.ViewHolder(view) {
        private var id: Int = 0
        init {
            view.checkboxSelect.setOnClickListener {
                presenter.cellSelected(id, (it as CheckBox).isChecked)
            }
        }
        fun inflate(position: Int) {
            view.title.text = resourceProvider.getString(presenter.getCell(position)?.data?.name)
            view.checkboxSelect.isChecked = presenter.isCellSelected(position)
            id = position
        }
    }
}
