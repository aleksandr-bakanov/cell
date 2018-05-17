package bav.onecell.celllogic.conditions

import bav.onecell.model.cell.logic.Condition
import io.reactivex.Observable

interface Conditions {

    interface View {
    }

    interface Presenter {
        /**
         * Presenter initializer
         *
         * @param cellIndex Index of cell to be working with
         */
        fun initialize(cellIndex: Int, ruleIndex: Int)

        fun conditionsUpdateNotifier(): Observable<Unit>

        fun conditionsCount(): Int

        fun createNewCondition()

        fun removeCondition(index: Int)

        fun openConditionEditor(conditionIndex: Int, whatToEdit: Int)

        fun provideActionDialogValues(): Array<String>
    }
}