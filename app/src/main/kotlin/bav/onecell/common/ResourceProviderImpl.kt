package bav.onecell.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import bav.onecell.R
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Action
import bav.onecell.model.cell.logic.Condition

class ResourceProviderImpl(private val context: Context): Common.ResourceProvider {

    override fun getActionRepresentationId(action: Action): Int {
        return when (action.act) {
            Action.Act.CHANGE_DIRECTION -> {
                when (action.value) {
                    Cell.Direction.N.ordinal -> R.drawable.ic_action_rotation_north
                    Cell.Direction.NE.ordinal -> R.drawable.ic_action_rotation_north_east
                    Cell.Direction.SE.ordinal -> R.drawable.ic_action_rotation_south_east
                    Cell.Direction.S.ordinal -> R.drawable.ic_action_rotation_south
                    Cell.Direction.SW.ordinal -> R.drawable.ic_action_rotation_south_west
                    Cell.Direction.NW.ordinal -> R.drawable.ic_action_rotation_north_west
                    else -> TRANSPARENT_HEX
                }
            }
        }
    }

    override fun getFieldToCheckRepresentationId(fieldToCheck: Condition.FieldToCheck): Int {
        return when (fieldToCheck) {
            Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> R.drawable.ic_field_to_check_direction_to_nearest_enemy
            Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> R.drawable.ic_field_to_check_distance_to_nearest_enemy
            else -> TRANSPARENT_HEX
        }
    }

    override fun getOperationRepresentationId(operation: Condition.Operation): Int {
        return when (operation) {
            Condition.Operation.EQUALS -> R.drawable.ic_operation_equals
            Condition.Operation.LESS_THAN -> R.drawable.ic_operation_less_than
            Condition.Operation.GREATER_THAN -> R.drawable.ic_operation_greater_than
            else -> TRANSPARENT_HEX
        }
    }

    override fun getExpectedValueRepresentationId(fieldToCheck: Condition.FieldToCheck, expected: Int): Int {
        return when (fieldToCheck) {
            Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> when (expected) {
                Cell.Direction.N.ordinal -> R.drawable.ic_expected_value_direction_north
                Cell.Direction.NE.ordinal -> R.drawable.ic_expected_value_direction_north_east
                Cell.Direction.SE.ordinal -> R.drawable.ic_expected_value_direction_south_east
                Cell.Direction.S.ordinal -> R.drawable.ic_expected_value_direction_south
                Cell.Direction.SW.ordinal -> R.drawable.ic_expected_value_direction_south_west
                Cell.Direction.NW.ordinal -> R.drawable.ic_expected_value_direction_north_west
                else -> TRANSPARENT_HEX
            }
            Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> when (expected) {
                0 -> R.drawable.ic_expected_value_0
                1 -> R.drawable.ic_expected_value_1
                2 -> R.drawable.ic_expected_value_2
                3 -> R.drawable.ic_expected_value_3
                4 -> R.drawable.ic_expected_value_4
                5 -> R.drawable.ic_expected_value_5
                6 -> R.drawable.ic_expected_value_6
                7 -> R.drawable.ic_expected_value_7
                8 -> R.drawable.ic_expected_value_8
                9 -> R.drawable.ic_expected_value_9
                else -> TRANSPARENT_HEX
            }
            else -> TRANSPARENT_HEX
        }
    }

    override fun getConditionRepresentation(condition: Condition): String {
        val ftc = getFieldToCheckRepresentationId(condition.fieldToCheck)
        val op = getOperationRepresentationId(condition.operation)
        val exp = getExpectedValueRepresentationId(condition.fieldToCheck, condition.expected)
        return context.resources.getString(R.string.condition_representation, ftc.toString(), op.toString(), exp.toString())
    }

    override fun getAvatarDrawable(index: Int): Drawable? {
        return ContextCompat.getDrawable(context, getAvatarDrawableId(index))
    }

    override fun getAvatarDrawableId(index: Int): Int {
        return when (index) {
            0 -> R.drawable.ic_avatar_kittaro
            1 -> R.drawable.ic_avatar_zoi
            2 -> R.drawable.ic_avatar_aima
            3, 4, 5 -> R.drawable.ic_avatar_gopnik_01
            6, 7, 8, 9, 10 -> R.drawable.ic_avatar_skilos_01
            11 -> R.drawable.ic_avatar_belos
            12 -> R.drawable.ic_avatar_omikhli
            13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 -> R.drawable.ic_avatar_nikhterib
            26 -> R.drawable.ic_avatar_drunkard_01
            27 -> R.drawable.ic_avatar_drunkard_02
            28 -> R.drawable.ic_avatar_drunkard_03
            29 -> R.drawable.ic_avatar_drunkard_04
            30 -> R.drawable.ic_avatar_drunkard_05
            31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43 -> R.drawable.ic_avatar_katofi_ponu_01
            44 -> R.drawable.ic_avatar_mage
            else -> R.drawable.ic_avatar_kittaro
        }
    }

    override fun getDrawableIdentifier(name: String?): Int = context.resources.getIdentifier(name, DRAWABLE, APP_PACKAGE)
    override fun getIdIdentifier(name: String?): Int = context.resources.getIdentifier(name, ID, APP_PACKAGE)
    override fun getStringIdentifier(name: String?): Int = context.resources.getIdentifier(name, STRING, APP_PACKAGE)

    override fun getDrawable(id: Int): Drawable? = ContextCompat.getDrawable(context, id)
    override fun getString(id: Int): String? = context.resources.getString(id)

    override fun getString(name: String?): String? = getString(getStringIdentifier(name))
    override fun getDrawable(name: String?): Drawable? = getDrawable(getDrawableIdentifier(name))

    override fun getColorIdentifier(name: String?): Int? = context.resources.getIdentifier(name, COLOR, APP_PACKAGE)
    override fun getColor(id: Int?): Int = id?.let { ContextCompat.getColor(context, it) } ?: Color.WHITE
    override fun getColor(name: String?): Int = if (name.isNullOrEmpty()) Color.WHITE else getColor(getColorIdentifier(name))

    companion object {
        private const val EMPTY_STRING = ""
        private val TRANSPARENT_HEX = R.drawable.ic_semi_transparent_hex
        private const val APP_PACKAGE = "bav.onecell"
        private const val DRAWABLE = "drawable"
        private const val ID = "id"
        private const val STRING = "string"
        private const val COLOR = "color"
    }
}