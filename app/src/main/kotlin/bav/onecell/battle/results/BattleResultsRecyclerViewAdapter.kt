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
import bav.onecell.databinding.ItemColumnInBattleResultsBinding

class BattleResultsRecyclerViewAdapter(private val presenter: BattleResults.Presenter,
                                       private val drawUtils: DrawUtils,
                                       private val resourceProvider: Common.ResourceProvider) :
        androidx.recyclerview.widget.RecyclerView.Adapter<BattleResultsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemColumnInBattleResultsBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding, presenter, drawUtils, resourceProvider)
    }

    override fun getItemCount() = presenter.groupsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder.binding.battleResultsGroupColumn.adapter as? BattleResultsColumnRecyclerViewAdapter)?.let {
            it.groupId = presenter.getGroupId(position)
        }
        holder.binding.battleResultsGroupColumn.adapter?.notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemColumnInBattleResultsBinding, presenter: BattleResults.Presenter, drawUtils: DrawUtils, resourceProvider: Common.ResourceProvider)
        : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        init {
            binding.battleResultsGroupColumn.layoutManager = LinearLayoutManager(binding.root.context)
            binding.battleResultsGroupColumn.adapter = BattleResultsColumnRecyclerViewAdapter(presenter, drawUtils, resourceProvider)
        }
    }

    companion object {
        private const val TAG = "BattleResultsAdapter"
    }
}
