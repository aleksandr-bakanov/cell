package bav.onecell.main

import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.model.RepositoryContract

class MainPresenter(
        private val view: Main.View?,
        private val gameState: Common.GameState,
        private val analytics: Common.Analytics,
        private val cellRepo: RepositoryContract.CellRepo) : Main.Presenter {

    override fun setDebugDecisions() {
        gameState.setDecision(Common.GameState.BATTLE_LOGIC_AVAILABLE, true)
        gameState.setDecision(Common.GameState.ATTACK_HEXES_AVAILABLE, true)
        gameState.setDecision(Common.GameState.ENERGY_HEXES_AVAILABLE, true)
        gameState.setDecision(Common.GameState.DEATH_RAY_HEXES_AVAILABLE, true)
        gameState.setDecision(Common.GameState.OMNI_BULLET_HEXES_AVAILABLE, true)
        gameState.setDecision(Common.GameState.HEX_TRANSFORMATION_AVAILABLE, true)
        gameState.setDecision(Common.GameState.ZOI_AVAILABLE, true)
        gameState.setDecision(Common.GameState.ALL_CHARACTERS_AVAILABLE, true)
        gameState.setDecision(Common.GameState.GAME_OVER, true)

        Consts.SceneId.values().forEach {
            gameState.setSceneAppeared(it.value)
        }
    }

    override fun isGameFinished(): Boolean = gameState.isDecisionPositive(Common.GameState.GAME_OVER)

    override fun showScenesButton(): Boolean = gameState.showScenesButton()

    override fun getLastNavDestinationId(): Int = gameState.getLastNavDestinationId()

    override fun sendBugReport() {
        val reportContent = arrayOf(Consts.KITTARO_INDEX, Consts.ZOI_INDEX, Consts.AIMA_INDEX)
                .map { i -> cellRepo.getCell(i)?.data?.toJson() }
                .joinToString("\n\n\n")
        view?.sendBugReport(reportContent)
    }

    companion object {
        private const val TAG = "MainPresenter"
    }
}