package bav.onecell.constructor

import bav.onecell.model.Cell

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
    }
}