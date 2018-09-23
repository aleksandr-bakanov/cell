package bav.onecell.celllogic.conditions

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Condition
import kotlinx.android.synthetic.main.item_row_add_new_condition.view.buttonAddNewCondition
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
                presenter.getCondition(position)?.let {
                    holder.view.buttonFieldToCheck.text = getFieldToCheckRepresentation(holder.view.context, it.fieldToCheck)
                    holder.view.buttonOperation.text = getOperationRepresentation(holder.view.context, it.operation)
                    holder.view.buttonExpectedValue.text = getExpectedValueRepresentation(holder.view.context, it.fieldToCheck, it.expected)
                }
            }
        }
    }

    private fun getFieldToCheckRepresentation(context: Context, fieldToCheck: Condition.FieldToCheck): String {
        return when (fieldToCheck) {
            Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> context.resources.getString(R.string.utf_icon_direction_to_nearest_enemy)
        }
    }

    private fun getOperationRepresentation(context: Context, operation: Condition.Operation): String {
        return when (operation) {
            Condition.Operation.EQUALS -> context.resources.getString(R.string.utf_icon_equality)
        }
    }

    private fun getExpectedValueRepresentation(context: Context, fieldToCheck: Condition.FieldToCheck, expected: Int): String {
        return when (fieldToCheck) {
            Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> when (expected) {
                Cell.Direction.N.ordinal -> context.resources.getString(R.string.utf_icon_north_direction)
                Cell.Direction.NE.ordinal -> context.resources.getString(R.string.utf_icon_north_east_direction)
                Cell.Direction.SE.ordinal -> context.resources.getString(R.string.utf_icon_south_east_direction)
                Cell.Direction.S.ordinal -> context.resources.getString(R.string.utf_icon_south_direction)
                Cell.Direction.SW.ordinal -> context.resources.getString(R.string.utf_icon_south_west_direction)
                Cell.Direction.NW.ordinal -> context.resources.getString(R.string.utf_icon_north_west_direction)
                else -> ""
            }
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
