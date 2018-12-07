package bav.onecell.battle.results

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.view.DrawUtils
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.preview
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.cellName
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.deadOrAlive

class BattleResultsColumnRecyclerViewAdapter(private val presenter: BattleResults.Presenter,
                                             private val drawUtils: DrawUtils,
                                             private val resourceProvider: Common.ResourceProvider) :
        androidx.recyclerview.widget.RecyclerView.Adapter<BattleResultsColumnRecyclerViewAdapter.ViewHolder>() {

    var groupId: Int = Consts.MAIN_CHARACTERS_GROUP_ID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell_in_battle_results, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount() = presenter.cellsCount(groupId)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        presenter.getCell(groupId, position)?.let {
            holder.view.preview.setImageResource(resourceProvider.getAvatarDrawableId(it.data.id.toInt()))
            holder.view.cellName.text = presenter.getCellName(it.data.name)
        }
        holder.view.deadOrAlive.setImageDrawable(
                if (presenter.getDeadOrAlive(groupId, position)) resourceProvider.getDrawable(R.drawable.ic_semi_transparent_hex)
                else resourceProvider.getDrawable(R.drawable.ic_remove_icon)
        )
    }

    class ViewHolder(val view: View, private val presenter: BattleResults.Presenter)
        : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

    companion object {
        private const val TAG = "ColumnAdapter"
    }
}
