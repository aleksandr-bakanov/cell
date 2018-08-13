package bav.onecell.common.router

import android.content.Context
import android.util.Log
import bav.onecell.R
import org.json.JSONException
import org.json.JSONObject

class SceneManagerImpl(private val router: Router, private val context: Context) : SceneManager {

    private val scenes: MutableMap<String, JSONObject> = mutableMapOf()
    private var currentScene: String = MAIN_MENU

    init {
        try {
            val scenario = JSONObject(context.resources.getString(R.string.scene_manager_scenario))
            val scenes = scenario.getJSONObject(SCENES)
            for (key in scenes.keys()) {
                this.scenes[key] = scenes.getJSONObject(key)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "wrong json: $e")
        }
    }

    //region Overridden methods
    override fun openMainMenu() {
        currentScene = MAIN_MENU
        router.goToMain()
    }

    override fun openIntroductionScene() {
        val cutSceneInfo = context.resources.getString(getStringIdentifier(scenes[INTRODUCTION]?.getString(PARAMS)))
        currentScene = INTRODUCTION
        router.goToCutScene(cutSceneInfo)
    }

    override fun openNextScene() {
        val nextScene = scenes[currentScene]?.getString(NEXT_SCENE)
        when (nextScene) {
            MAIN_MENU -> openMainMenu()
        }
    }
    //endregion

    private fun getStringIdentifier(name: String?): Int = context.resources.getIdentifier(name, "string", "bav.onecell")

    companion object {
        private const val SCENES = "scenes"
        private const val WINDOW = "window"
        private const val PARAMS = "params"
        private const val NEXT_SCENE = "next_scene"

        private const val INTRODUCTION = "introduction"
        private const val MAIN_MENU = "main_menu"

        private const val TAG = "SceneManager"
    }
}
