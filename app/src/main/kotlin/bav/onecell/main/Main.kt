package bav.onecell.main

import io.reactivex.Observable

interface Main {

    interface View {
        fun informAboutBugReportFilePath(path: String, content: String)
    }

    interface Presenter {
        fun setDebugDecisions()
        fun isGameFinished(): Boolean
        fun getLastNavDestinationId(): Int
        fun sendBugReport()
    }

    interface NavigationInfoProvider {
        fun provideLastDestination(): Observable<Int>
    }
}
