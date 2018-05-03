package bav.onecell.celllogic

import io.reactivex.Observable

interface CellLogic {

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

        fun openConditionsEditor(ruleIndex: Int)

        fun conditionsNotifier(): Observable<Unit>

        fun conditionsCount(): Int

        fun createNewCondition()

        fun removeCondition(index: Int)
    }
}