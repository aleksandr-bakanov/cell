package bav.onecell.celllogic.picker

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import kotlinx.android.synthetic.main.item_row_cell_logic_picker_option.view.buttonOption

class PickerRecyclerViewAdapter(private val presenter: Picker.Presenter) :
        RecyclerView.Adapter<PickerRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell_logic_picker_option, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount(): Int = presenter.pickerOptionsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.buttonOption.text =
                holder.view.context.resources.getString(presenter.getPickerOptionTitle(position))
    }

    class ViewHolder(val view: View, private val presenter: Picker.Presenter) : RecyclerView.ViewHolder(view) {
        init {
            view.buttonOption.setOnClickListener { presenter.pickerOptionOnClick(adapterPosition) }
        }
    }
}
