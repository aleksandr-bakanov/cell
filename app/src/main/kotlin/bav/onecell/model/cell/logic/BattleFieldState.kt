package bav.onecell.model.cell.logic

import bav.onecell.model.cell.Cell

/**
 * This class represents state of battle field and contains several properties based on which cell logic
 * mechanism can decide which action should be performed.
 *
 * There are common and individual for each cell properties.
 */
class BattleFieldState {
    // Individual properties
    var directionToNearestEnemy: Cell.Direction = Cell.Direction.N

    // Common properties

    // Private properties
    val directions = mutableListOf<Int>()
    val rads = mutableListOf<Float>()
}
