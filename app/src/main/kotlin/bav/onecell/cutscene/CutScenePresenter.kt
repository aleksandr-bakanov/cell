package bav.onecell.cutscene

import bav.onecell.common.router.SceneManager

class CutScenePresenter(private val sceneManager: SceneManager) : CutScene.Presenter {

    override fun openNextScene() {
        sceneManager.openNextScene()
    }

    companion object {
        private const val TAG = "CutScenePresenter"
    }

}