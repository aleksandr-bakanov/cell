package bav.onecell.celllogic.conditions

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import bav.onecell.common.Common
import kotlinx.android.synthetic.main.item_row_add_new_condition.view.buttonAddNewCondition
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonExpectedValue
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonFieldToCheck
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonOperation
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonRemoveCondition
import kotlinx.android.synthetic.main.item_row_rule_condition.view.conditionRow

class ConditionsRecyclerViewAdapter(
        private val presenter: Conditions.Presenter,
        private val resourceProvider: Common.ResourceProvider) : RecyclerView.Adapter<ConditionsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view, presenter, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == presenter.conditionsCount()) R.layout.item_row_add_new_condition
        else R.layout.item_row_rule_condition
    }

    override fun getItemCount(): Int = presenter.conditionsCount() + 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_row_add_new_condition -> {
                // Do nothing
            }
            R.layout.item_row_rule_condition -> {
                presenter.getCondition(position)?.let {
                    holder.view.conditionRow.setBackgroundColor(getRowBackgroundColor(holder.view.context, position))
                    holder.view.buttonFieldToCheck.text = resourceProvider.getFieldToCheckRepresentation(it.fieldToCheck)
                    holder.view.buttonOperation.text = resourceProvider.getOperationRepresentation(it.operation)
                    holder.view.buttonExpectedValue.text = resourceProvider.getExpectedValueRepresentation(it.fieldToCheck, it.expected)
                }
            }
        }
    }

    private fun getRowBackgroundColor(context: Context, position: Int): Int {
        return if (position == presenter.getCurrentConditionIndex())
            ContextCompat.getColor(context, R.color.heroScreenSelectedConditionBackgroundColor)
        else ContextCompat.getColor(context, R.color.heroScreenUnselectedConditionBackgroundColor)
    }

    class ViewHolder(val view: View, private val presenter: Conditions.Presenter, viewType: Int) : RecyclerView.ViewHolder(view) {
        init {
            when (viewType) {
                R.layout.item_row_add_new_condition -> {
                    view.buttonAddNewCondition.setOnClickListener { presenter.createNewCondition() }
                }
                R.layout.item_row_rule_condition -> {
                    view.buttonRemoveCondition.setOnClickListener { presenter.removeCondition(adapterPosition) }
                    view.buttonFieldToCheck.setOnClickListener { presenter.chooseFieldToCheck(adapterPosition) }
                    view.buttonOperation.setOnClickListener { presenter.chooseOperation(adapterPosition) }
                    view.buttonExpectedValue.setOnClickListener { presenter.chooseExpectedValue(adapterPosition) }
                }
            }
        }
    }
}
