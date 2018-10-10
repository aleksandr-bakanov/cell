package bav.onecell.main

import bav.onecell.common.router.Router
import bav.onecell.common.router.SceneManager

class MainPresenter(
        private val router: Router, private val sceneManager: SceneManager) : Main.Presenter {

    companion object {
        private const val TAG = "MainPresenter"
    }

    override fun openPreBattleView() {
        router.goToCellsForBattleSelection()
    }

    override fun startNewGame(info: String) {
        sceneManager.openIntroductionScene()
    }

    override fun openHeroScreen() {
        router.goToHeroesScreen()
    }

    //endregion
}