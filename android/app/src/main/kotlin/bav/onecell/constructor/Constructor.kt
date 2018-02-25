package bav.onecell.constructor

interface Constructor {

    interface View {

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