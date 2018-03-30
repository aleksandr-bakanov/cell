package bav.onecell.constructor

import bav.onecell.model.Cell
import bav.onecell.model.hexes.Hex

interface Constructor {

    interface View {
        fun setBackgroundFieldRadius(radius: Int)
        fun setCell(cell: Cell?)
    }

    interface Presenter {
        /**
         * Presenter initializer
         *
         * @param cellIndex Index of cell to be working with
         */
        fun initialize(cellIndex: Int)

        /**
         * Add new hex to cell
         *
         * @param hex Hex to add
         */
        fun addHexToCell(hex: Hex)

        /**
         * Remove hex from cell
         *
         * @param hex Hex to remove
         */
        fun removeHexFromCell(hex: Hex)

        fun rotateCellLeft()

        fun rotateCellRight()
    }
}