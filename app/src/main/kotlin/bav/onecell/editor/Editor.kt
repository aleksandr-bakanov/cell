package bav.onecell.editor

import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import io.reactivex.Observable

interface Editor {

    interface View {
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

        fun getCellProvider(): Observable<Cell>

        fun getBackgroundCellRadiusProvider(): Observable<Int>
    }
}