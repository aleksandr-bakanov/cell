package bav.onecell.cellslist

import io.reactivex.Observable

interface CellsList {
    interface View {
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
         * Opens cell editor view for defined cell
         *
         * @param cellIndex Cell index in repository
         */
        fun openCellEditor(cellIndex: Int)

        /**
         * Opens cell rules editor view for defined cell
         *
         * @param cellIndex Cell index in repository
         */
        fun openCellRulesEditor(cellIndex: Int)

        /**
         * Removes cell from repository
         *
         * @param cellIndex Index of cell in repository
         */
        fun removeCell(cellIndex: Int)

        /**
         * Presenter initializer
         */
        fun initialize()

        /**
         * Provides cell repository updates notifier. It will emit Unit on each cell's addition/removing.
         *
         * @return Observable to notify about cell repository content changes
         */
        fun cellRepoUpdateNotifier(): Observable<Unit>

        //region Lifecycle events
        fun onPause()
        //endregion
    }
}