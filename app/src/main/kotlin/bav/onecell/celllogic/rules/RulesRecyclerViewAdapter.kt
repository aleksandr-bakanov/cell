package bav.onecell.celllogic.rules

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Action
import kotlinx.android.synthetic.main.item_row_add_new_rule.view.buttonAddNewRule
import kotlinx.android.synthetic.main.item_row_rule.view.buttonChooseRuleAction
import kotlinx.android.synthetic.main.item_row_rule.view.buttonOpenRuleConditions
import kotlinx.android.synthetic.main.item_row_rule.view.buttonRemoveRule
import kotlinx.android.synthetic.main.item_row_rule.view.title

class RulesRecyclerViewAdapter(private val presenter: Rules.Presenter) :
        RecyclerView.Adapter<RulesRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view, presenter, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == presenter.rulesCount()) R.layout.item_row_add_new_rule
               else R.layout.item_row_rule
    }

    override fun getItemCount(): Int = presenter.rulesCount() + 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_row_add_new_rule -> {
                // Do nothing
            }
            R.layout.item_row_rule -> {
                holder.view.title.text = "Rule #$position"
                presenter.getRule(position)?.let {
                    holder.view.buttonChooseRuleAction.text = getActionRepresentation(it.action)
                }
            }
        }
    }

    private fun getActionRepresentation(action: Action): String {
        return when (action.act) {
            Action.Act.CHANGE_DIRECTION -> {
                when (action.value) {
                    Cell.Direction.N.ordinal -> "↑"
                    Cell.Direction.NE.ordinal -> "↗"
                    Cell.Direction.SE.ordinal -> "↘"
                    Cell.Direction.S.ordinal -> "↓"
                    Cell.Direction.SW.ordinal -> "↙"
                    Cell.Direction.NW.ordinal -> "↖"
                    else -> ""
                }
            }
        }
    }

    class ViewHolder(val view: View, private val presenter: Rules.Presenter, viewType: Int) : RecyclerView.ViewHolder(view) {
        init {
            when (viewType) {
                R.layout.item_row_add_new_rule -> {
                    view.buttonAddNewRule.setOnClickListener { presenter.createNewRule() }
                }
                R.layout.item_row_rule -> {
                    view.buttonRemoveRule.setOnClickListener { presenter.removeRule(adapterPosition) }
                    view.buttonOpenRuleConditions.setOnClickListener { presenter.openConditionsList(adapterPosition) }
                    view.buttonChooseRuleAction.setOnClickListener { presenter.openActionEditor(adapterPosition) }
                }
            }
        }
    }
}
