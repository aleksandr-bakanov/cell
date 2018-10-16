package bav.onecell.model.cell.logic

import bav.onecell.model.cell.Cell

/**
 * This class represents condition which form rules.
 */
class Condition(var operation: Operation = Operation.EQUALS,
                var fieldToCheck: FieldToCheck = FieldToCheck.DIRECTION_TO_NEAREST_ENEMY,
                var expected: Int = Cell.Direction.N.ordinal) {

    enum class Operation {
        EQUALS, LESS_THAN, GREATER_THAN;

        companion object {
            private val map = Operation.values().associateBy { it.ordinal }
            fun fromInt(type: Int): Operation = map[type] ?: EQUALS
        }
    }

    enum class FieldToCheck {
        DIRECTION_TO_NEAREST_ENEMY, DISTANCE_TO_NEAREST_ENEMY;

        companion object {
            private val map = FieldToCheck.values().associateBy { it.ordinal }
            fun fromInt(type: Int): FieldToCheck = map[type] ?: DIRECTION_TO_NEAREST_ENEMY
        }
    }

    fun check(state: BattleFieldState): Boolean {
        val fieldToCheckValue: Int = when (fieldToCheck) {
            FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> state.directionToNearestEnemy.ordinal
            FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> state.distanceToNearestEnemy
        }
        return when (operation) {
            Operation.EQUALS -> fieldToCheckValue == expected
            Operation.LESS_THAN -> fieldToCheckValue < expected
            Operation.GREATER_THAN -> fieldToCheckValue > expected
        }
    }

    fun setToDefault() {
        operation = Operation.EQUALS
        expected = NO_DATA
    }

    companion object {
        const val NO_DATA = -1
    }
}
