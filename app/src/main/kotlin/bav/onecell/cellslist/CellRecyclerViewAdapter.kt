package bav.onecell.cellslist

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import bav.onecell.R
import bav.onecell.common.view.DrawUtils
import kotlinx.android.synthetic.main.item_row_cell.view.buttonEditCell
import kotlinx.android.synthetic.main.item_row_cell.view.buttonEditCellRules
import kotlinx.android.synthetic.main.item_row_cell.view.buttonRemoveCell
import kotlinx.android.synthetic.main.item_row_cell.view.preview
import kotlinx.android.synthetic.main.item_row_cell.view.title

class CellRecyclerViewAdapter(private val presenter: CellsList.Presenter, private val drawUtils: DrawUtils) :
        androidx.recyclerview.widget.RecyclerView.Adapter<CellRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount() = presenter.cellsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setCellTitle(presenter.getCellName(position))

        presenter.getCell(position)?.let {
            holder.previewBitmap.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(holder.previewBitmap)
            val layout = drawUtils.provideLayout(canvas, it.size() * 2)
            drawUtils.drawCell(canvas, it, layout = layout)
            holder.view.preview.invalidate()
        }
    }

    class ViewHolder(val view: View, private val presenter: CellsList.Presenter,
                     val previewBitmap: Bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888))
        : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

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
            view.title.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { presenter.setCellName(adapterPosition, it.toString()) }
                }

                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            })
            view.preview.setImageBitmap(previewBitmap)
        }

        fun setCellTitle(title: String) {
            view.title.setText(title, TextView.BufferType.EDITABLE)
        }
    }
}
