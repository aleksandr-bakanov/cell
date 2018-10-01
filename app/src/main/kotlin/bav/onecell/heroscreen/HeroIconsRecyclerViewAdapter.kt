package bav.onecell.heroscreen

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import kotlinx.android.synthetic.main.item_row_hero_avatar.view.avatar

class HeroIconsRecyclerViewAdapter(
        private val presenter: HeroScreen.Presenter): RecyclerView.Adapter<HeroIconsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_hero_avatar, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount(): Int = presenter.getCellCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    companion object {
        const val GAP_BETWEEN_ITEMS = 4
    }

    class ViewHolder(val view: View, private val presenter: HeroScreen.Presenter) : RecyclerView.ViewHolder(view) {
        init {
            view.avatar.setOnClickListener { presenter.initialize(adapterPosition) }
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
