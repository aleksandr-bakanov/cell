package bav.onecell.celllogic.conditions

import bav.onecell.model.cell.logic.Condition

interface ConditionEditor {

    interface View {

    }

    interface Presenter {
        fun initialize(condition: Condition, whatToEdit: Int)

        fun provideConditionDialogValues(): Array<String>

        fun saveConditionValue(which: Int)
    }
}