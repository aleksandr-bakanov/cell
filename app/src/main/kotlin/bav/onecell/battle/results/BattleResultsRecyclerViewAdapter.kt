package bav.onecell.battle.results

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import bav.onecell.common.Consts
import bav.onecell.common.view.DrawUtils
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.preview
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.cellName
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.deadOrAlive
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.dealtDamage

class BattleResultsRecyclerViewAdapter(private val presenter: BattleResults.Presenter,
                                       private val drawUtils: DrawUtils,
                                       private val groupId: Int = Consts.MAIN_CHARACTERS_GROUP_ID) :
        androidx.recyclerview.widget.RecyclerView.Adapter<BattleResultsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell_in_battle_results, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount() = presenter.cellsCount(groupId)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        presenter.getCell(groupId, position)?.let {
            holder.previewBitmap.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(holder.previewBitmap)
            val layout = drawUtils.provideLayout(canvas, it.size() * 2)
            drawUtils.drawCell(canvas, it, layout = layout)
            holder.view.preview.invalidate()

            holder.view.cellName.text = presenter.getCellName(it.data.name)
        }
        holder.view.dealtDamage.text = presenter.getDealtDamage(groupId, position).toString()
        holder.view.deadOrAlive.isChecked = presenter.getDeadOrAlive(groupId, position)
    }

    class ViewHolder(val view: View, private val presenter: BattleResults.Presenter,
                     val previewBitmap: Bitmap = Bitmap.createBitmap(PREVIEW_BITMAP_SIZE, PREVIEW_BITMAP_SIZE,
                                                                     Bitmap.Config.ARGB_8888))
        : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        init {
            view.preview.setImageBitmap(previewBitmap)
        }
    }

    companion object {
        const val PREVIEW_BITMAP_SIZE = 300
    }
}
