package bav.onecell.celllogic.conditions

import android.content.Context
import android.view.Gravity
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.heroscreen.HeroScreen
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonExpectedValue
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonFieldToCheck
import kotlinx.android.synthetic.main.item_row_rule_condition.view.buttonOperation
import kotlinx.android.synthetic.main.item_row_rule_condition.view.conditionRow

class ConditionsRecyclerViewAdapter(
        private val presenter: HeroScreen.Presenter,
        private val resourceProvider: Common.ResourceProvider) : androidx.recyclerview.widget.RecyclerView.Adapter<ConditionsRecyclerViewAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view, presenter, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_row_rule_condition
    }

    override fun getItemCount(): Int = if (presenter.conditionsCount() == -1) 0 else presenter.conditionsCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_row_rule_condition -> {
                presenter.getCondition(position)?.let {
                    holder.view.conditionRow.setBackgroundColor(getRowBackgroundColor(holder.view.context, position))
                    holder.view.buttonFieldToCheck.setImageResource(resourceProvider.getFieldToCheckRepresentationId(it.fieldToCheck))
                    holder.view.buttonOperation.setImageResource(resourceProvider.getOperationRepresentationId(it.operation))
                    holder.view.buttonExpectedValue.setImageResource(resourceProvider.getExpectedValueRepresentationId(it.fieldToCheck, it.expected))
                }
            }
        }
    }

    private fun getRowBackgroundColor(context: Context, position: Int): Int {
        return if (position == presenter.getCurrentConditionIndex())
            ContextCompat.getColor(context, R.color.heroScreenSelectedConditionBackgroundColor)
        else ContextCompat.getColor(context, R.color.heroScreenUnselectedConditionBackgroundColor)
    }

    override fun onItemDismiss(position: Int) {
        presenter.removeCondition(position)
    }

    class ViewHolder(val view: View, private val presenter: HeroScreen.Presenter, viewType: Int) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        init {
            when (viewType) {
                R.layout.item_row_rule_condition -> {
                    view.buttonFieldToCheck.setOnClickListener {
                        presenter.chooseFieldToCheck(adapterPosition)
                        showPopupMenu(it, R.menu.condition_field_to_check)
                    }
                    view.buttonOperation.setOnClickListener {
                        showPopupMenu(it, presenter.chooseOperation(adapterPosition))
                    }
                    view.buttonExpectedValue.setOnClickListener {
                        showPopupMenu(it, presenter.chooseExpectedValue(adapterPosition))
                    }
                }
            }
        }

        private fun showPopupMenu(view: View, menuLayout: Int) {
            if (menuLayout != 0) {
                val popupMenu = PopupMenu(view.context, view)
                forceIconsShow(popupMenu)
                popupMenu.inflate(menuLayout)
                popupMenu.setOnMenuItemClickListener(menuItemClickListener)
                popupMenu.show()
            }
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
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return makeMovementFlags(0, swipeFlags)
        }

        override fun isItemViewSwipeEnabled(): Boolean = true

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            adapter.onItemDismiss(viewHolder.adapterPosition)
        }
    }
}

interface ItemTouchHelperAdapter {
    fun onItemDismiss(position: Int)
}
