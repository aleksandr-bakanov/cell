package bav.onecell.heroscreen

import android.support.v7.widget.RecyclerView
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

    class ViewHolder(val view: View, private val presenter: HeroScreen.Presenter) : RecyclerView.ViewHolder(view) {
        init {
            view.avatar.setOnClickListener { presenter.initialize(adapterPosition) }
        }
    }
}
