package bav.onecell.common

import android.content.Context
import bav.onecell.R
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Action
import bav.onecell.model.cell.logic.Condition

class ResourceProviderImpl(private val context: Context): Common.ResourceProvider {

    override fun getActionRepresentation(action: Action): String {
        return when (action.act) {
            Action.Act.CHANGE_DIRECTION -> {
                when (action.value) {
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
    }

    override fun getFieldToCheckRepresentation(fieldToCheck: Condition.FieldToCheck): String {
        return when (fieldToCheck) {
            Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> context.resources.getString(R.string.utf_icon_direction_to_nearest_enemy)
        }
    }

    override fun getOperationRepresentation(operation: Condition.Operation): String {
        return when (operation) {
            Condition.Operation.EQUALS -> context.resources.getString(R.string.utf_icon_equality)
        }
    }

    override fun getExpectedValueRepresentation(fieldToCheck: Condition.FieldToCheck, expected: Int): String {
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

    override fun getConditionRepresentation(condition: Condition): String {
        val ftc = getFieldToCheckRepresentation(condition.fieldToCheck)
        val op = getOperationRepresentation(condition.operation)
        val exp = getExpectedValueRepresentation(condition.fieldToCheck, condition.expected)
        return context.resources.getString(R.string.condition_representation, ftc, op, exp)
    }
}