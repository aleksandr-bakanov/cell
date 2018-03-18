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
         * @return cells count
         */
        fun cellsCount(): Int

        /**
         * Opens cell constructor view for defined cell
         *
         * @param cellIndex cell index in repository
         */
        fun openCellConstructor(cellIndex: Int)
    }
}