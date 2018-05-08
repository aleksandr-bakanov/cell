package bav.onecell.celllogic

import bav.onecell.model.cell.logic.Condition
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

        fun actionEditNotifier(): Observable<Unit>

        fun openConditionsList(ruleIndex: Int)

        fun openActionEditor(ruleIndex: Int)

        fun conditionsUpdateNotifier(): Observable<Unit>

        fun conditionsEditNotifier(): Observable<Condition>

        fun conditionsCount(): Int

        fun createNewCondition()

        fun removeCondition(index: Int)

        fun openConditionEditor(conditionIndex: Int, whatToEdit: Int)

        fun provideConditionDialogValues(): Array<String>

        fun provideActionDialogValues(): Array<String>

        fun saveConditionValue(which: Int)

        fun saveActionValue(which: Int)

        fun saveCondition()
    }

    interface PresenterProvider {
        fun provideCellLogicPresenter(): CellLogic.Presenter
    }
}