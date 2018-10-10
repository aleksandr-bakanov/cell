package bav.onecell.main

interface Main {

    interface View {

    }

    interface Presenter {
        /**
         * Opens battle window.
         */
        fun openPreBattleView()

        fun startNewGame(info: String)

        fun openHeroScreen()
    }
}
