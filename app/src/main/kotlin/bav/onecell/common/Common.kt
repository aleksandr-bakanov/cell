package bav.onecell.common

import bav.onecell.model.cell.logic.Action
import bav.onecell.model.cell.logic.Condition

interface Common {
    interface ResourceProvider {
        fun getActionRepresentation(action: Action): String

        fun getFieldToCheckRepresentation(fieldToCheck: Condition.FieldToCheck): String

        fun getOperationRepresentation(operation: Condition.Operation): String

        fun getExpectedValueRepresentation(fieldToCheck: Condition.FieldToCheck, expected: Int): String

        fun getConditionRepresentation(condition: Condition): String
    }
}