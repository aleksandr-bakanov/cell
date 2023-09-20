package bav.onecell.heroscreen

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.databinding.ItemRowHeroAvatarBinding

class HeroIconsRecyclerViewAdapter(
        private val presenter: HeroScreen.Presenter,
        private val resourceProvider: Common.ResourceProvider): RecyclerView.Adapter<HeroIconsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRowHeroAvatarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_hero_avatar, parent, false)
        return ViewHolder(binding, presenter)
    }

    override fun getItemCount(): Int = presenter.getCellCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.avatar.setImageResource(resourceProvider.getAvatarDrawableId(position))
    }

    companion object {
        const val GAP_BETWEEN_ITEMS = 4
    }

    class ViewHolder(val binding: ItemRowHeroAvatarBinding, private val presenter: HeroScreen.Presenter) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.avatar.setOnClickListener { presenter.initialize(adapterPosition) }
        }
    }

    class VerticalSpaceItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            parent.adapter?.let {
                if (parent.getChildAdapterPosition(view) != it.itemCount - 1) outRect.bottom = GAP_BETWEEN_ITEMS
            }
        }
    }
}
