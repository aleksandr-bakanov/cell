package bav.onecell.model

import bav.onecell.model.hexes.Hex

/**
 * This class contains functions to check possibility to add hexes into cell
 */
class Rules {
    companion object {
        fun isAllowedToAddHexIntoCell(cell: Cell, hex: Hex): Boolean {
            val hexes = cell.hexes
            // First of all we need to check whether this hex is a first one in cell,
            // then it will be allowed only if it is life hex.
            if (hexes.size == 0) return hex.type == Hex.Type.LIFE

            return false
        }
    }
}