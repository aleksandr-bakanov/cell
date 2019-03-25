package bav.onecell.main

import io.reactivex.Observable

interface Main {

    interface View {

    }

    interface Presenter {
        fun setDebugDecisions()
        fun isGameFinished(): Boolean
        fun getLastNavDestinationId(): Int
    }

    interface NavigationInfoProvider {
        fun provideLastDestination(): Observable<Int>
    }
}
