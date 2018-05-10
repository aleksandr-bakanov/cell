package bav.onecell.main

import io.reactivex.Observable

interface Main {

    interface View {
        /**
         * Provide information about screen orientation
         *
         * @return True if orientation is landscape, false if portrait
         */
        fun isLandscape(): Boolean

        /**
         * Opens cell editor view
         *
         * @param cellIndex Index of cell in repository
         */
        fun openCellEditorView(cellIndex: Int)

        /**
         * Opens cell logic editor view
         *
         * @param cellIndex Index of cell in repository
         */
        fun openCellLogicEditorView(cellIndex: Int)
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
         * Opens battle window, passing list of cell indexes within repository to it.
         *
         * @param cellIndexes Cell indexes in repository
         */
        fun openBattleView(cellIndexes: List<Int>)

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
