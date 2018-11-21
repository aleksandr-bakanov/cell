package bav.onecell.celllogic.rules

import android.content.Context
import android.view.Gravity
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.heroscreen.HeroScreen
import bav.onecell.model.cell.logic.Condition
import kotlinx.android.synthetic.main.item_row_add_new_rule.view.buttonAddNewRule
import kotlinx.android.synthetic.main.item_row_rule.view.buttonChooseRuleAction
import kotlinx.android.synthetic.main.item_row_rule.view.buttonRemoveRule
import kotlinx.android.synthetic.main.item_row_rule.view.ruleRow
import kotlinx.android.synthetic.main.item_row_rule.view.title

class RulesRecyclerViewAdapter(
        private val presenter: HeroScreen.Presenter,
        private val resourceProvider: Common.ResourceProvider) : androidx.recyclerview.widget.RecyclerView.Adapter<RulesRecyclerViewAdapter.ViewHolder>() {

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
                holder.view.ruleRow.setBackgroundColor(getRowBackgroundColor(holder.view.context, position))
                presenter.getRule(position)?.let {
                    holder.view.title.text = getConditionsRepresentation(it.getConditions())
                    holder.view.buttonChooseRuleAction.setImageResource(resourceProvider.getActionRepresentationId(it.action))
                }
            }
        }
    }

    private fun getConditionsRepresentation(conditions: List<Condition>): String {
        return conditions.joinToString(" ") { resourceProvider.getConditionRepresentation(it) }
    }

    private fun getRowBackgroundColor(context: Context, position: Int): Int {
        return if (position == presenter.getCurrentlySelectedRuleIndex())
            ContextCompat.getColor(context, R.color.heroScreenSelectedRuleBackgroundColor)
            else ContextCompat.getColor(context, R.color.heroScreenUnselectedRuleBackgroundColor)
    }

    class ViewHolder(val view: View, private val presenter: HeroScreen.Presenter, viewType: Int) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        init {
            when (viewType) {
                R.layout.item_row_add_new_rule -> {
                    view.buttonAddNewRule.setOnClickListener { presenter.createNewRule() }
                }
                R.layout.item_row_rule -> {
                    view.buttonRemoveRule.setOnClickListener { presenter.removeRule(adapterPosition) }
                    view.ruleRow.setOnClickListener { presenter.openConditionsList(adapterPosition) }
                    view.buttonChooseRuleAction.setOnClickListener {
                        presenter.openActionEditor(adapterPosition)
                        showPopupMenu(view.context, view, R.menu.rules_actions)
                    }
                }
            }
        }

        private fun showPopupMenu(context: Context, view: View, menuLayout: Int) {
            val popupMenu = PopupMenu(context, view)
            forceIconsShow(popupMenu)
            popupMenu.inflate(menuLayout)
            popupMenu.setOnMenuItemClickListener(menuItemClickListener)
            popupMenu.show()
        }

        // From here: https://readyandroid.wordpress.com/popup-menu-with-icon/
        private fun forceIconsShow(popup: PopupMenu) {
            try {
                val fields = popup.javaClass.declaredFields
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field.get(popup)
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcons = classPopupHelper.getMethod("setForceShowIcon",
                                                                       Boolean::class.javaPrimitiveType)
                        setForceIcons.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        private val menuItemClickListener = PopupMenu.OnMenuItemClickListener {
            presenter.pickerOptionOnClick(it.itemId)
            true
        }
    }
}
