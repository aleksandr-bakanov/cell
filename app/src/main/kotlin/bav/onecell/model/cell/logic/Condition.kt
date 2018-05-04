package bav.onecell.model.cell.logic

import bav.onecell.model.cell.Cell

/**
 * This class represents condition which form rules.
 */
class Condition(var operation: Operation = Operation.EQUALS,
                var fieldToCheck: FieldToCheck = FieldToCheck.DIRECTION_TO_NEAREST_ENEMY,
                var expected: Any = Cell.Direction.N) {

    enum class Operation(value: String) {
        EQUALS("eq")
    }

    enum class FieldToCheck(value: String) {
        DIRECTION_TO_NEAREST_ENEMY("dirToNearEnemy")
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
