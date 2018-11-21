package bav.onecell.model.cell.logic

/**
 * This class represents condition which form rules.
 */
class Condition(var operation: Operation = Operation.NO_DATA,
                var fieldToCheck: FieldToCheck = FieldToCheck.NO_DATA,
                var expected: Int = NO_DATA) {

    enum class Operation {
        NO_DATA, EQUALS, LESS_THAN, GREATER_THAN;

        companion object {
            private val map = Operation.values().associateBy { it.ordinal }
            fun fromInt(type: Int): Operation = map[type] ?: NO_DATA
        }
    }

    enum class FieldToCheck {
        NO_DATA, DIRECTION_TO_NEAREST_ENEMY, DISTANCE_TO_NEAREST_ENEMY;

        companion object {
            private val map = FieldToCheck.values().associateBy { it.ordinal }
            fun fromInt(type: Int): FieldToCheck = map[type] ?: NO_DATA
        }
    }

    fun check(state: BattleFieldState): Boolean {
        val fieldToCheckValue: Int = when (fieldToCheck) {
            FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> state.directionToNearestEnemy.ordinal
            FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> state.distanceToNearestEnemy
            else -> NO_DATA
        }
        return when (operation) {
            Operation.EQUALS -> fieldToCheckValue == expected
            Operation.LESS_THAN -> fieldToCheckValue < expected
            Operation.GREATER_THAN -> fieldToCheckValue > expected
            else -> false
        }
    }

    fun setToDefault() {
        operation = Operation.NO_DATA
        expected = NO_DATA
    }

    companion object {
        const val NO_DATA = -1
    }
}
