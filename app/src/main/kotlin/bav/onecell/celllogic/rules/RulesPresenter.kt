package bav.onecell.celllogic.rules

import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Rule
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RulesPresenter(private val cellRepository: RepositoryContract.CellRepo) : Rules.Presenter {

    companion object {
        private const val TAG = "RulesPresenter"
    }

    private var rules: MutableList<Rule>? = null
    private val rulesNotifier = PublishSubject.create<Unit>()
    private val actionNotifier = PublishSubject.create<Unit>()

    private var currentlyEditedRuleIndex: Int = -1
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

    override fun actionEditNotifier(): Observable<Unit> = actionNotifier

    override fun openConditionsList(ruleIndex: Int) {
        currentlyEditedRule = rules?.let {
            if (ruleIndex >= 0 && ruleIndex < it.size) {
                currentlyEditedRuleIndex = ruleIndex
                it[ruleIndex]
            } else null
        }
    }

    override fun openActionEditor(ruleIndex: Int) {
        currentlyEditedRule = rules?.let {
            if (ruleIndex >= 0 && ruleIndex < it.size) {
                currentlyEditedRuleIndex = ruleIndex
                it[ruleIndex]
            } else null
        }
        actionNotifier.onNext(Unit)
    }

    private val directionValues: Array<String> = arrayOf(
            Cell.Direction.N.toString(),
            Cell.Direction.NE.toString(),
            Cell.Direction.SE.toString(),
            Cell.Direction.S.toString(),
            Cell.Direction.SW.toString(),
            Cell.Direction.NW.toString()
    )

    override fun provideActionDialogValues(): Array<String> {
        return directionValues
    }

    override fun saveActionValue(which: Int) {
        currentlyEditedRule?.action?.value = directionValues[which]
    }
}
