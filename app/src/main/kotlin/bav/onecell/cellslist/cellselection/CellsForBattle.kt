package bav.onecell.cellslist.cellselection

import io.reactivex.Observable

interface CellsForBattle {
    interface View {
    }

    interface Presenter {
        /**
         * Return cells count in repository
         *
         * @return Cells count
         */
        fun cellsCount(): Int

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

        fun startBattle(cellIndexes: List<Int>)
    }
}
