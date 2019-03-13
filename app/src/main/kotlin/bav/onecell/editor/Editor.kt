package bav.onecell.editor

import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import io.reactivex.Observable

interface Editor {

    interface View {
        fun highlightTips(type: Hex.Type)
        fun updateCellRepresentation()
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

        /**
         * Rotates edited cell left (CCW) by 60 degrees
         */
        fun rotateCellLeft()

        /**
         * Rotates edited cell right (CW) by 60 degrees
         */
        fun rotateCellRight()

        /**
         * Provides source of cell to edit
         *
         * @return Observable which emit cells to edit
         */
        fun getCellProvider(): Observable<Cell>

        /**
         * Provides source of background field radius to be drawn in editor view
         *
         * @return Observable which emit background field radius
         */
        fun getBackgroundCellRadiusProvider(): Observable<Int>

        fun getTipHexes(type: Hex.Type): Collection<Hex>

        fun getHexInBucketCount(type: Hex.Type): Int
    }
}