package bav.onecell.celllogic

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R

import kotlinx.android.synthetic.main.item_row_rule.view.*

class RulesRecyclerViewAdapter(private val presenter: CellLogic.Presenter) :
        RecyclerView.Adapter<RulesRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_rule, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount(): Int = presenter.rulesCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setRuleTitle("Rule #$position")
        holder.index = position
    }

    class ViewHolder(val view: View, private val presenter: CellLogic.Presenter) : RecyclerView.ViewHolder(view) {
        var index: Int = 0

        init {
            view.buttonRemoveRule.setOnClickListener { presenter.removeRule(index) }
            view.buttonEditRule.setOnClickListener { presenter.openConditionsList(index) }
        }

        fun setRuleTitle(title: String) {
            view.title.text = title
        }
    }
}
