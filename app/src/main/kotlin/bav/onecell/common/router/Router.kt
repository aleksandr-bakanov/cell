package bav.onecell.common.router

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import bav.onecell.model.cell.logic.Condition
import io.reactivex.Observable

interface Router {

    data class Window(val type: WindowType, val args: Bundle? = null)

    enum class WindowType {
        MAIN, CELLS_LIST, BATTLE_CELLS_SELECTION, BATTLE, CELL_EDITOR, RULES_EDITOR, CONDITIONS_EDITOR, CUT_SCENE,
        BATTLE_RESULTS, HERO_SCREEN;

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

    fun goToCellsList()

    fun goToCellEditor(index: Int)

    fun goToRulesList(index: Int)

    fun goToConditionList(cellIndex: Int, ruleIndex: Int)

    fun goToActionEditor(cellIndex: Int, ruleIndex: Int)

    fun setHostActivity(activity: FragmentActivity)

    fun goToConditionEditor(condition: Condition, whatToEdit: Int)

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