package bav.onecell.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.crashlytics.android.Crashlytics

class GameStateImpl(private val context: Context,
                    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)): Common.GameState {

    private var skipSaveLastNavDestination = false
    private var isIgnoreCutSceneShownStatus = false

    override fun dropGameState() {
        preferences.edit().clear().apply()
        isIgnoreCutSceneShownStatus = false
    }

    override fun getLastNavDestinationId(): Int {
        val id = preferences.getInt(LAST_NAV_DESTINATION_ID, 0)
        Crashlytics.log("GameState::getLastNavDestinationId id = $id)")
        return id
    }
    override fun setLastNavDestinationId(id: Int, skipNext: Boolean) {
        if (!skipSaveLastNavDestination) {
            Crashlytics.log("GameState::setLastNavDestinationId(id = $id)")
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

    override fun setCutSceneShown(cutSceneId: String) = preferences.edit().putBoolean(cutSceneId, true).apply()
    override fun isCutSceneAlreadyShown(cutSceneId: String): Boolean = preferences.getBoolean(cutSceneId, false)

    override fun setIgnoreCutSceneShownStatus(status: Boolean) {
        isIgnoreCutSceneShownStatus = status
    }
    override fun getIgnoreCutSceneShownStatus(): Boolean = isIgnoreCutSceneShownStatus

    override fun setSceneAppeared(sceneId: String) {
        val editor = preferences.edit()
        var i = 0
        while (scenesOrder[i] != sceneId) {
            editor.putBoolean("${scenesOrder[i]}_appeared", true)
            i++
        }
        editor.putBoolean("${sceneId}_appeared", true).putBoolean(SHOW_SCENES_BUTTON, true).apply()
    }
    override fun isSceneAppeared(sceneId: String): Boolean = preferences.getBoolean("${sceneId}_appeared", false)

    override fun showScenesButton(): Boolean = preferences.getBoolean(SHOW_SCENES_BUTTON, false)

    private val scenesOrder = arrayListOf(
            Consts.SceneId.INTRODUCTION.value,
            Consts.SceneId.BATTLE_GOPNIKS.value,
            Consts.SceneId.AFTER_GOPNIKS.value,
            Consts.SceneId.BEFORE_SKILOS.value,
            Consts.SceneId.BATTLE_SKILOS.value,
            Consts.SceneId.AFTER_SKILOS.value,
            Consts.SceneId.GONATO.value,
            Consts.SceneId.BATTLE_BELOS.value,
            Consts.SceneId.AFTER_BELOS.value,
            Consts.SceneId.ANALAFRO.value,
            Consts.SceneId.BATTLE_OMIKHLI.value,
            Consts.SceneId.AFTER_OMIKHLI.value,
            Consts.SceneId.KILIA.value,
            Consts.SceneId.BATTLE_NIKHTERIBS.value,
            Consts.SceneId.AFTER_NIKHTERIBS.value,
            Consts.SceneId.KARDIA.value,
            Consts.SceneId.BATTLE_DRUNKARDS.value,
            Consts.SceneId.AFTER_DRUNKARDS.value,
            Consts.SceneId.LAIMO.value,
            Consts.SceneId.BATTLE_KATOFI_PONU.value,
            Consts.SceneId.AFTER_KATOFI_PONU.value,
            Consts.SceneId.ENKEFALIO.value,
            Consts.SceneId.BATTLE_ENKEFALIO.value,
            Consts.SceneId.FINAL_ACT.value
    )

    companion object {
        private const val FIRST_TIME_APP_LAUNCH = "first_time_app_launch"
        private const val LAST_NAV_DESTINATION_ID = "last_nav_destination_id"
        private const val CURRENT_FRAME_INDEX = "current_frame_index"
        private const val SHOW_SCENES_BUTTON = "show_scenes_button"
    }
}
