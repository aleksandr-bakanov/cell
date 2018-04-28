package bav.onecell.model.cell.logic

import bav.onecell.model.cell.Cell

/**
 * This class represents action to be performed on cell.
 */
class Action(private val act: Act = Act.CHANGE_DIRECTION,
             private val value: Any = Cell.Direction.N) {

    enum class Act(value: String) {
        CHANGE_DIRECTION("change_direction")
    }

    fun perform(cell: Cell) {
        when (act) {
            Act.CHANGE_DIRECTION -> cell.data.direction = (value as? Cell.Direction) ?: Cell.Direction.N
        }
    }
}
