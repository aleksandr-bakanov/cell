package bav.onecell.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class GameStateImpl(private val context: Context,
                    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)): Common.GameState {

    private var skipSaveLastNavDestination = false

    override fun dropGameState() {
        preferences.edit().clear().putBoolean(FIRST_TIME_APP_LAUNCH, false).apply()
    }

    override fun getLastNavDestinationId(): Int = preferences.getInt(LAST_NAV_DESTINATION_ID, 0)
    override fun setLastNavDestinationId(id: Int, skipNext: Boolean) {
        if (!skipSaveLastNavDestination) {
            preferences.edit().putInt(LAST_NAV_DESTINATION_ID, id).apply()
        }
        else {
            skipSaveLastNavDestination = false
        }
        skipSaveLastNavDestination = skipNext
    }

    override fun isFirstLaunch(): Boolean {
        val firstLaunch = preferences.getBoolean(FIRST_TIME_APP_LAUNCH, true)
        if (firstLaunch) preferences.edit().putBoolean(FIRST_TIME_APP_LAUNCH, false).apply()
        return firstLaunch
    }

    override fun setDecision(field: String, value: Boolean) {
        val decision = if (value) Common.GameState.Decision.YES else Common.GameState.Decision.NO
        preferences.edit().putInt(field, decision.ordinal).apply()
    }

    override fun getDecision(field: String): Common.GameState.Decision {
        val decision = preferences.getInt(field, Common.GameState.Decision.NOT_TAKEN.ordinal)
        return when (decision) {
            Common.GameState.Decision.YES.ordinal -> Common.GameState.Decision.YES
            Common.GameState.Decision.NO.ordinal -> Common.GameState.Decision.NO
            else -> Common.GameState.Decision.NOT_TAKEN
        }
    }

    override fun isDecisionPositive(field: String): Boolean = getDecision(field) == Common.GameState.Decision.YES

    override fun setCurrentFrame(index: Int) = preferences.edit().putInt(CURRENT_FRAME_INDEX, index).apply()

    override fun getCurrentFrame(): Int = preferences.getInt(CURRENT_FRAME_INDEX, 0)

    companion object {
        private const val FIRST_TIME_APP_LAUNCH = "first_time_app_launch"
        private const val LAST_NAV_DESTINATION_ID = "last_nav_destination_id"
        private const val CURRENT_FRAME_INDEX = "current_frame_index"
    }
}
