package bav.onecell.common.router

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import bav.onecell.model.cell.logic.Condition
import io.reactivex.Observable

interface Router {

    data class Window(val type: WindowType, val args: Bundle? = null)

    enum class WindowType {
        MAIN, BATTLE_CELLS_SELECTION, BATTLE, CELL_EDITOR, CUT_SCENE, BATTLE_RESULTS, HERO_SCREEN;

        companion object {
            private val map = WindowType.values().associateBy { it.toString() }
            fun fromString(type: String): WindowType = map[type] ?: MAIN
        }
    }

    fun windowChange(): Observable<Window>

    fun goBack()

    fun goToMain()

    fun goToCellsForBattleSelection()

    fun goToBattle(cellIndexes: List<Int>)

    fun setHostActivity(activity: FragmentActivity)

    fun goToCutScene(cutSceneInfo: String)

    fun goToBattleResults(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>)

    fun goToHeroesScreen()
}

interface SceneManager {
    fun openMainMenu()

    fun openIntroductionScene()

    fun openBattleWithGopniks()

    fun openNextScene()
}