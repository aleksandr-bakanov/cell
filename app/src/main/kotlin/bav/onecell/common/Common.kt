package bav.onecell.common

import android.graphics.drawable.Drawable
import bav.onecell.model.cell.logic.Action
import bav.onecell.model.cell.logic.Condition

interface Common {
    interface ResourceProvider {
        fun getActionRepresentation(action: Action): String

        fun getFieldToCheckRepresentation(fieldToCheck: Condition.FieldToCheck): String

        fun getOperationRepresentation(operation: Condition.Operation): String

        fun getExpectedValueRepresentation(fieldToCheck: Condition.FieldToCheck, expected: Int): String

        fun getConditionRepresentation(condition: Condition): String

        fun getAvatarDrawable(index: Int): Drawable?

        fun getAvatarDrawableId(index: Int): Int

        fun getDrawableIdentifier(name: String?): Int

        fun getIdIdentifier(name: String?): Int
    }

    interface GameState {
        fun getLastNavDestinationId(): Int
        fun setLastNavDestinationId(id: Int)

        fun isFirstLaunch(): Boolean
    }
}