package bav.onecell.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class GameStateImpl(private val context: Context,
                    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)): Common.GameState {

    override fun getLastNavDestinationId(): Int = preferences.getInt(LAST_NAV_DESTINATION_ID, 0)
    override fun setLastNavDestinationId(id: Int) = preferences.edit().putInt(LAST_NAV_DESTINATION_ID, id).apply()

    override fun isFirstLaunch(): Boolean {
        val firstLaunch = preferences.getBoolean(FIRST_TIME_APP_LAUNCH, true)
        if (firstLaunch) preferences.edit().putBoolean(FIRST_TIME_APP_LAUNCH, false).apply()
        return firstLaunch
    }

    companion object {
        private const val FIRST_TIME_APP_LAUNCH = "first_time_app_launch"
        private const val LAST_NAV_DESTINATION_ID = "last_nav_destination_id"
    }
}