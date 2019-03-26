package bav.onecell.common

import android.app.Activity
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsImpl(context: Context): Common.Analytics {

    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun setCurrentScreen(activity: Activity, screenName: String?, screenClassOverride: String?) {
        //firebaseAnalytics.setCurrentScreen(activity, screenName, screenClassOverride)
    }
}
