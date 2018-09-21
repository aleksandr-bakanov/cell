package bav.onecell.celllogic.conditions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import bav.onecell.model.cell.logic.Condition
import kotlinx.android.synthetic.main.item_row_add_new_condition.view.buttonAddNewCondition
import kotlinx.android.synthetic.main.item_row_rule.view.title
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonExpectedValue
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonFieldToCheck
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonOperation
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonRemoveCondition

class ConditionsRecyclerViewAdapter(private val presenter: Conditions.Presenter) :
        RecyclerView.Adapter<ConditionsRecyclerViewAdapter.ViewHolder>() {

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
                holder.view.title.text = "#$position"
                presenter.getCondition(position)?.let {
                    holder.view.buttonFieldToCheck.text = getFieldToCheckRepresentation(it.fieldToCheck)
                }
            }
        }
    }

    private fun getFieldToCheckRepresentation(fieldToCheck: Condition.FieldToCheck): String {
        return when (fieldToCheck) {
            Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> "\uD83D\uDE08"
        }
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
