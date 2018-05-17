package bav.onecell.common.router

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import bav.onecell.celllogic.conditions.ConditionEditorDialogFragment
import bav.onecell.celllogic.conditions.ConditionListFragment
import bav.onecell.celllogic.rules.RuleListFragment
import bav.onecell.editor.EditorFragment
import bav.onecell.model.cell.logic.Condition
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class RouterImpl : Router {

    private lateinit var host: FragmentActivity
    private val windowChanger = BehaviorSubject.create<Router.Window>()

    override fun windowChange(): Observable<Router.Window> = windowChanger

    override fun goToMain() {
        windowChanger.onNext(Router.Window(Router.WindowType.MAIN))
    }

    override fun goToBattle() {

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
        val conditionEditorDialogFragment = ConditionEditorDialogFragment.newInstance(condition, whatToEdit)
        conditionEditorDialogFragment.show(host.fragmentManager, ConditionListFragment.CONDITION_EDITOR_DIALOG_TAG)
    }
}
