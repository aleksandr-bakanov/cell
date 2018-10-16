package bav.onecell.common

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
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
            Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> context.resources.getString(R.string.utf_icon_distance_to_nearest_enemy)
        }
    }

    override fun getOperationRepresentation(operation: Condition.Operation): String {
        return when (operation) {
            Condition.Operation.EQUALS -> context.resources.getString(R.string.utf_icon_equality)
            Condition.Operation.LESS_THAN -> context.resources.getString(R.string.utf_icon_less_than)
            Condition.Operation.GREATER_THAN -> context.resources.getString(R.string.utf_icon_greater_than)
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
            Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> when (expected) {
                0 -> context.resources.getString(R.string.utf_icon_digit_zero)
                1 -> context.resources.getString(R.string.utf_icon_digit_one)
                2 -> context.resources.getString(R.string.utf_icon_digit_two)
                3 -> context.resources.getString(R.string.utf_icon_digit_three)
                4 -> context.resources.getString(R.string.utf_icon_digit_four)
                5 -> context.resources.getString(R.string.utf_icon_digit_five)
                6 -> context.resources.getString(R.string.utf_icon_digit_six)
                7 -> context.resources.getString(R.string.utf_icon_digit_seven)
                8 -> context.resources.getString(R.string.utf_icon_digit_eight)
                9 -> context.resources.getString(R.string.utf_icon_digit_nine)
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

    override fun getAvatarDrawable(index: Int): Drawable? {
        return when (index) {
            0 -> ContextCompat.getDrawable(context, R.drawable.ic_avatar_kittaro)
            1 -> ContextCompat.getDrawable(context, R.drawable.ic_avatar_zoi)
            2 -> ContextCompat.getDrawable(context, R.drawable.ic_avatar_aima)
            3, 4, 5 -> ContextCompat.getDrawable(context, R.drawable.ic_avatar_gopnik_01)
            6, 7, 8, 9, 10 -> ContextCompat.getDrawable(context, R.drawable.ic_avatar_skilos_01)
            else -> null
        }
    }

    override fun getAvatarDrawableId(index: Int): Int {
        return when (index) {
            0 -> R.drawable.ic_avatar_kittaro
            1 -> R.drawable.ic_avatar_zoi
            2 -> R.drawable.ic_avatar_aima
            3, 4, 5 -> R.drawable.ic_avatar_gopnik_01
            6, 7, 8, 9, 10 -> R.drawable.ic_avatar_skilos_01
            else -> R.drawable.ic_avatar_kittaro
        }
    }
}