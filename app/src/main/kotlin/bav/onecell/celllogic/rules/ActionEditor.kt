package bav.onecell.celllogic.rules

interface ActionEditor {

    interface View {

    }

    interface Presenter {
        fun initialize(cellIndex: Int, ruleIndex: Int)

        fun provideActionDialogValues(): Array<String>

        fun saveActionValue(which: Int)
    }
}