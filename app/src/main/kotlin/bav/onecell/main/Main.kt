package bav.onecell.main

import io.reactivex.Observable

interface Main {

    interface View {
        fun isDualPane(): Boolean
        fun openEditorFragment(cellIndex: Int)
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
         * Opens battle window, passing list of cell indexes within repository to it.
         *
         * @param cellIndexes Cell indexes in repository
         */
        fun openBattleView(cellIndexes: List<Int>)

        fun removeCell(cellIndex: Int)

        fun onPause()

        fun initialize()

        fun cellRepoUpdateNotifier(): Observable<Unit>
    }
}