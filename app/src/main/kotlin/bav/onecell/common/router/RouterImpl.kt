package bav.onecell.common.router

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import bav.onecell.battle.BattleFragment
import bav.onecell.battle.results.BattleResultsFragment
import bav.onecell.celllogic.conditions.ConditionEditorDialogFragment
import bav.onecell.celllogic.conditions.ConditionListFragment
import bav.onecell.celllogic.rules.ActionEditorDialogFragment
import bav.onecell.celllogic.rules.RuleListFragment
import bav.onecell.cutscene.CutSceneFragment
import bav.onecell.editor.EditorFragment
import bav.onecell.model.cell.logic.Condition
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RouterImpl : Router {

    private lateinit var host: FragmentActivity
    private val windowChanger = PublishSubject.create<Router.Window>()

    override fun windowChange(): Observable<Router.Window> = windowChanger

    override fun goBack() {
        host.supportFragmentManager.popBackStack()
    }

    override fun goToMain() {
        windowChanger.onNext(Router.Window(Router.WindowType.MAIN))
    }

    override fun goToCellsForBattleSelection() {
        windowChanger.onNext(Router.Window(Router.WindowType.BATTLE_CELLS_SELECTION))
    }

    override fun goToBattle(cellIndexes: List<Int>) {
        val bundle = Bundle()
        bundle.putIntegerArrayList(BattleFragment.EXTRA_CELL_INDEXES, ArrayList(cellIndexes))
        windowChanger.onNext(Router.Window(Router.WindowType.BATTLE, bundle))
    }

    override fun goToCellsList() {
        windowChanger.onNext(Router.Window(Router.WindowType.CELLS_LIST))
    }

    override fun goToCellEditor(index: Int) {
        val bundle = Bundle()
        bundle.putInt(EditorFragment.CELL_INDEX, index)
        windowChanger.onNext(Router.Window(Router.WindowType.CELL_EDITOR, bundle))
    }

    override fun goToRulesList(index: Int) {
        val bundle = Bundle()
        bundle.putInt(RuleListFragment.CELL_INDEX, index)
        windowChanger.onNext(Router.Window(Router.WindowType.RULES_EDITOR, bundle))
    }

    override fun goToConditionList(cellIndex: Int, ruleIndex: Int) {
        val bundle = Bundle()
        bundle.putInt(ConditionListFragment.CELL_INDEX, cellIndex)
        bundle.putInt(ConditionListFragment.RULE_INDEX, ruleIndex)
        windowChanger.onNext(Router.Window(Router.WindowType.CONDITIONS_EDITOR, bundle))
    }

    override fun setHostActivity(activity: FragmentActivity) {
        host = activity
    }

    override fun goToConditionEditor(condition: Condition, whatToEdit: Int) {
        val fragment = ConditionEditorDialogFragment.newInstance(condition, whatToEdit)
        fragment.show(host.fragmentManager, ConditionEditorDialogFragment.CONDITION_EDITOR_DIALOG_TAG)
    }

    override fun goToActionEditor(cellIndex: Int, ruleIndex: Int) {
        val fragment = ActionEditorDialogFragment.newInstance(cellIndex, ruleIndex)
        fragment.show(host.fragmentManager, ActionEditorDialogFragment.ACTION_EDITOR_DIALOG_TAG)
    }

    override fun goToCutScene(cutSceneInfo: String) {
        val bundle = Bundle()
        bundle.putString(CutSceneFragment.INFO_JSON, cutSceneInfo)
        windowChanger.onNext(Router.Window(Router.WindowType.CUT_SCENE, bundle))
    }

    override fun goToBattleResults(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>) {
        val bundle = Bundle()
        bundle.putIntArray(BattleResultsFragment.CELL_INDEXES, dealtDamage.keys.toIntArray())
        bundle.putIntArray(BattleResultsFragment.DEALT_DAMAGE, dealtDamage.values.toIntArray())
        val doa = arrayListOf<Boolean>()
        dealtDamage.keys.forEach { doa.add(deadOrAliveCells[it] ?: false) }
        bundle.putBooleanArray(BattleResultsFragment.DEAD_OR_ALIVE, doa.toBooleanArray())
        windowChanger.onNext(Router.Window(Router.WindowType.BATTLE_RESULTS, bundle))
    }

    companion object {
        private const val TAG = "RouterImpl"
    }
}
