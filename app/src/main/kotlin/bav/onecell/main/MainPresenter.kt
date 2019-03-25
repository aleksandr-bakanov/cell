package bav.onecell.main

import bav.onecell.common.Common

class MainPresenter(
        private val gameState: Common.GameState,
        private val analytics: Common.Analytics) : Main.Presenter {

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
    }

    override fun isGameFinished(): Boolean = gameState.isDecisionPositive(Common.GameState.GAME_OVER)

    override fun getLastNavDestinationId(): Int = gameState.getLastNavDestinationId()

    companion object {
        private const val TAG = "MainPresenter"
    }
}