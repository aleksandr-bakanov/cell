package bav.onecell.main

interface Main {

    interface View {
        /**
         * Update cell repository list view
         */
        fun notifyCellRepoListUpdated()
    }

    interface Presenter {
        /**
         * Creates new cell an stores it in cell repository
         */
        fun createNewCell()

        /**
         * Return cells count in repository
         *
         * @return Cells count
         */
        fun cellsCount(): Int

        /**
         * Opens cell constructor view for defined cell
         *
         * @param cellIndex Cell index in repository
         */
        fun openCellConstructor(cellIndex: Int)

        /**
         * Opens battle window, passing list of cell indexes within repository to it.
         *
         * @param cellIndexes Cell indexes in repository
         */
        fun openBattleView(cellIndexes: List<Int>)

        fun removeCell(cellIndex: Int)

        fun onPause()
    }
}