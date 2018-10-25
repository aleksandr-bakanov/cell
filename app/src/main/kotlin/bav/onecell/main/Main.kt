package bav.onecell.main

import io.reactivex.Observable

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

    interface NavigationInfoProvider {
        fun provideLastDestination(): Observable<Int>
    }
}
