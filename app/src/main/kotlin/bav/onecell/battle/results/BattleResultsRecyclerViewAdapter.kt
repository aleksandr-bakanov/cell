package bav.onecell.battle.results

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.view.DrawUtils
import kotlinx.android.synthetic.main.item_column_in_battle_results.view.battleResultsGroupColumn

class BattleResultsRecyclerViewAdapter(private val presenter: BattleResults.Presenter,
                                       private val drawUtils: DrawUtils,
                                       private val resourceProvider: Common.ResourceProvider) :
        androidx.recyclerview.widget.RecyclerView.Adapter<BattleResultsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_column_in_battle_results, parent, false)
        return ViewHolder(view, presenter, drawUtils, resourceProvider)
    }

    override fun getItemCount() = presenter.groupsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder.view.battleResultsGroupColumn.adapter as? BattleResultsColumnRecyclerViewAdapter)?.let {
            it.groupId = presenter.getGroupId(position)
        }
        holder.view.battleResultsGroupColumn.adapter?.notifyDataSetChanged()
    }

    class ViewHolder(val view: View, presenter: BattleResults.Presenter, drawUtils: DrawUtils, resourceProvider: Common.ResourceProvider)
        : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        init {
            view.battleResultsGroupColumn.layoutManager = LinearLayoutManager(view.context)
            view.battleResultsGroupColumn.adapter = BattleResultsColumnRecyclerViewAdapter(presenter, drawUtils, resourceProvider)
        }
    }

    companion object {
        private const val TAG = "BattleResultsAdapter"
    }
}
