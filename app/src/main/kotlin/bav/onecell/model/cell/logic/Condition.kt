package bav.onecell.model.cell.logic

import bav.onecell.model.cell.Cell

/**
 * This class represents condition which form rules.
 */
class Condition(var operation: Operation = Operation.EQUALS,
                var fieldToCheck: FieldToCheck = FieldToCheck.DIRECTION_TO_NEAREST_ENEMY,
                var expected: Any = Cell.Direction.N) {

    enum class Operation(val value: String) {
        EQUALS("eq");

        companion object {
            private val map = Operation.values().associateBy { it.value }
            fun fromString(type: String): Operation = map[type] ?: EQUALS
        }
    }

    enum class FieldToCheck(val value: String) {
        DIRECTION_TO_NEAREST_ENEMY("dirToNearEnemy");

        companion object {
            private val map = FieldToCheck.values().associateBy { it.value }
            fun fromString(type: String): FieldToCheck = map[type] ?: DIRECTION_TO_NEAREST_ENEMY
        }
    }

    fun check(state: BattleState): Boolean {
        val fieldToCheckValue: Any = when (fieldToCheck) {
            FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> state.directionToNearestEnemy
        }
        return when (operation) {
            Operation.EQUALS -> fieldToCheckValue == expected
        }
    }
}
