package bav.onecell.celllogic.rules

import android.content.Context
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.heroscreen.HeroScreen
import bav.onecell.model.cell.logic.Condition
import kotlinx.android.synthetic.main.item_row_rule.view.buttonChooseRuleAction
import kotlinx.android.synthetic.main.item_row_rule.view.buttonRemoveRule
import kotlinx.android.synthetic.main.item_row_rule.view.conditions
import kotlinx.android.synthetic.main.item_row_rule.view.ruleRow
import kotlinx.android.synthetic.main.view_conditions_list.view.ruleConditionListItemExpectedValue
import kotlinx.android.synthetic.main.view_conditions_list.view.ruleConditionListItemFieldToCheck
import kotlinx.android.synthetic.main.view_conditions_list.view.ruleConditionListItemOperation

class RulesRecyclerViewAdapter(
        private val presenter: HeroScreen.Presenter,
        private val resourceProvider: Common.ResourceProvider)
    : androidx.recyclerview.widget.RecyclerView.Adapter<RulesRecyclerViewAdapter.ViewHolder>(),
      ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view, presenter, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_row_rule
    }

    override fun getItemCount(): Int = presenter.rulesCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_row_rule -> {
                holder.view.ruleRow.setBackgroundColor(getRowBackgroundColor(holder.view.context, position))
                presenter.getRule(position)?.let {
                    inflateConditions(holder.view.conditions, it.getConditions())
                    holder.view.buttonChooseRuleAction.setImageResource(resourceProvider.getActionRepresentationId(it.action))
                }
            }
        }
    }

    private fun inflateConditions(container: LinearLayoutCompat, conditions: List<Condition>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(container.context)
        for (condition in conditions) {
            val layout = inflater.inflate(R.layout.view_conditions_list, null, false)
            layout.ruleConditionListItemFieldToCheck.setImageResource(resourceProvider.getFieldToCheckRepresentationId(condition.fieldToCheck))
            layout.ruleConditionListItemOperation.setImageResource(resourceProvider.getOperationRepresentationId(condition.operation))
            layout.ruleConditionListItemExpectedValue.setImageResource(resourceProvider.getExpectedValueRepresentationId(condition.fieldToCheck, condition.expected))
            container.addView(layout)
        }
    }

    private fun getRowBackgroundColor(context: Context, position: Int): Int {
        return if (position == presenter.getCurrentlySelectedRuleIndex())
            ContextCompat.getColor(context, R.color.heroScreenSelectedRuleBackgroundColor)
            else ContextCompat.getColor(context, R.color.heroScreenUnselectedRuleBackgroundColor)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                presenter.swapRules(i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                presenter.swapRules(i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        presenter.removeRule(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(val view: View, private val presenter: HeroScreen.Presenter, viewType: Int) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        init {
            when (viewType) {
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

    class SimpleItemTouchHelperCallback(private val adapter: ItemTouchHelperAdapter): ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun isLongPressDragEnabled(): Boolean = true

        override fun isItemViewSwipeEnabled(): Boolean = true

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            adapter.onItemDismiss(viewHolder.adapterPosition)
        }
    }
}

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
    fun onItemDismiss(position: Int)
}
