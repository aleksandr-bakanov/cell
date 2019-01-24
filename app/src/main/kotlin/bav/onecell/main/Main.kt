package bav.onecell.main

import io.reactivex.Observable

interface Main {

    interface View {

    }

    interface Presenter {

    }

    interface NavigationInfoProvider {
        fun provideLastDestination(): Observable<Int>
    }
}
