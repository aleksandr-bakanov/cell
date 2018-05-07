package bav.onecell.celllogic

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import kotlinx.android.synthetic.main.item_row_rule.view.title
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonExpectedValue
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonFieldToCheck
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonOperation
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonRemoveCondition

class ConditionsRecyclerViewAdapter(private val presenter: CellLogic.Presenter) :
        RecyclerView.Adapter<ConditionsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_rule_condition, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount(): Int = presenter.conditionsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setRuleTitle("#$position")
        holder.index = position
    }

    class ViewHolder(val view: View, private val presenter: CellLogic.Presenter) : RecyclerView.ViewHolder(view) {
        var index: Int = 0

        init {
            view.buttonRemoveCondition.setOnClickListener { presenter.removeCondition(index) }
            view.buttonFieldToCheck.setOnClickListener {
                presenter.openConditionEditor(index, CellLogicPresenter.ConditionPartToEdit.FIELD.value)
            }
            view.buttonOperation.setOnClickListener {
                presenter.openConditionEditor(index, CellLogicPresenter.ConditionPartToEdit.OPERATION.value)
            }
            view.buttonExpectedValue.setOnClickListener {
                presenter.openConditionEditor(index, CellLogicPresenter.ConditionPartToEdit.EXPECTED.value)
            }
        }

        fun setRuleTitle(title: String) {
            view.title.text = title
        }
    }
}
