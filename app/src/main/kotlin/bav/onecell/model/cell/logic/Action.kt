package bav.onecell.model.cell.logic

import bav.onecell.model.cell.Cell

/**
 * This class represents action to be performed on cell.
 */
class Action(var act: Act = Act.CHANGE_DIRECTION,
             var value: Int = Cell.Direction.N.ordinal) {

    enum class Act {
        CHANGE_DIRECTION;

        companion object {
            private val map = Act.values().associateBy { it.ordinal }
            fun fromInt(type: Int): Act = map[type] ?: CHANGE_DIRECTION
        }
    }

    fun perform(cell: Cell) {
        when (act) {
            Act.CHANGE_DIRECTION -> cell.rotate(Cell.Direction.fromInt(value))
        }
    }
}
