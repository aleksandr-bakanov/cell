package bav.onecell.battle.results

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import bav.onecell.common.view.DrawUtils
import kotlinx.android.synthetic.main.item_row_cell.view.preview

class BattleResultsRecyclerViewAdapter(private val presenter: BattleResults.Presenter,
                                       private val drawUtils: DrawUtils) :
        RecyclerView.Adapter<BattleResultsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount() = presenter.cellsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        presenter.getCell(position)?.let {
            holder.previewBitmap.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(holder.previewBitmap)
            val layout = drawUtils.provideLayout(canvas, it.size() * 2)
            drawUtils.drawCell(canvas, it, layout = layout)
            holder.view.preview.invalidate()
        }
    }

    class ViewHolder(val view: View, private val presenter: BattleResults.Presenter,
                     val previewBitmap: Bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888))
        : RecyclerView.ViewHolder(view) {

        init {
            view.preview.setImageBitmap(previewBitmap)
        }
    }
}
