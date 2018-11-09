package bav.onecell.cellslist.cellselection

import bav.onecell.model.cell.Cell
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

        fun getCell(index: Int): Cell?

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

        fun cellSelected(index: Int, selected: Boolean)

        fun isCellSelected(index: Int): Boolean

        fun getSelectedCells(): List<Int>
    }
}
