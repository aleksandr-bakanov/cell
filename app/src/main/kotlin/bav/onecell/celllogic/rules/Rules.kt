package bav.onecell.celllogic.rules

import bav.onecell.model.cell.logic.Rule
import io.reactivex.Observable

interface Rules {

    interface View {

    }

    interface Presenter {
        /**
         * Presenter initializer
         *
         * @param cellIndex Index of cell to be working with
         */
        fun initialize(cellIndex: Int)

        /**
         * Return count of rules for selected cell
         *
         * @return Count of rules for cell
         */
        fun rulesCount(): Int

        /**
         * Creates new rule an stores it in cell
         */
        fun createNewRule()

        fun removeRule(index: Int)

        fun rulesUpdateNotifier(): Observable<Unit>

        fun openConditionsList(ruleIndex: Int)

        fun openActionEditor(ruleIndex: Int)

        fun getRule(index: Int): Rule?
    }
}