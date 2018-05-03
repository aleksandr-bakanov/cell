package bav.onecell.celllogic

import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.logic.Condition
import bav.onecell.model.cell.logic.Rule
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class CellLogicPresenter(
        private val cellRepository: RepositoryContract.CellRepo) : CellLogic.Presenter {

    companion object {
        private const val TAG = "CellLogicPresenter"
    }

    private var rules: MutableList<Rule>? = null
    private val rulesNotifier = PublishSubject.create<Unit>()
    private val conditionsNotifier = PublishSubject.create<Unit>()
    private var currentlyEditedRuleIndex: Int = 0
    private var currentlyEditedRule: Rule? = null

    override fun initialize(cellIndex: Int) {
        rules = cellRepository.getCell(cellIndex)?.data?.rules
    }

    override fun rulesCount(): Int = rules?.size ?: 0

    override fun createNewRule() {
        rules?.let {
            it.add(Rule())
            rulesNotifier.onNext(Unit)
        }
    }

    override fun removeRule(index: Int) {
        rules?.let {
            if (index in 0 until it.size) it.removeAt(index)
            if (index == currentlyEditedRuleIndex) currentlyEditedRule = null
            rulesNotifier.onNext(Unit)
        }
    }

    override fun rulesUpdateNotifier(): Observable<Unit> = rulesNotifier

    override fun conditionsNotifier(): Observable<Unit> = conditionsNotifier

    override fun openConditionsEditor(ruleIndex: Int) {
        currentlyEditedRule = rules?.let {
            if (ruleIndex >= 0 && ruleIndex < it.size) it[ruleIndex]
            else null
        }
        conditionsNotifier.onNext(Unit)
    }

    override fun conditionsCount(): Int = currentlyEditedRule?.size() ?: 0

    override fun createNewCondition() {
        currentlyEditedRule?.addCondition(Condition())
        conditionsNotifier.onNext(Unit)
    }

    override fun removeCondition(index: Int) {
        currentlyEditedRule?.removeConditionAt(index)
        conditionsNotifier.onNext(Unit)
    }
}
