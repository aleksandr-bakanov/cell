package bav.onecell.main

interface Main {

    interface View {

    }

    interface Presenter {
        /**
         * Opens battle window.
         */
        fun openPreBattleView()

        /**
         * Opens cells list window.
         */
        fun openCellsListView()
    }
}
