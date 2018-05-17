package bav.onecell.celllogic.rules

import bav.onecell.common.router.Router
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.logic.Rule
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RulesPresenter(private val cellRepository: RepositoryContract.CellRepo,
                     private val router: Router) : Rules.Presenter {

    companion object {
        private const val TAG = "RulesPresenter"
    }

    private var rules: MutableList<Rule>? = null
    private val rulesNotifier = PublishSubject.create<Unit>()

    private var currentlyEditedCellIndex: Int = -1

    override fun initialize(cellIndex: Int) {
        rules = cellRepository.getCell(cellIndex)?.data?.rules
        rules?.let { currentlyEditedCellIndex = cellIndex }
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
            rulesNotifier.onNext(Unit)
        }
    }

    override fun rulesUpdateNotifier(): Observable<Unit> = rulesNotifier

    override fun openConditionsList(ruleIndex: Int) {
        rules?.let {
            if (ruleIndex >= 0 && ruleIndex < it.size) {
                router.goToConditionList(currentlyEditedCellIndex, ruleIndex)
            }
        }
    }

    override fun openActionEditor(ruleIndex: Int) {
        rules?.let {
            if (ruleIndex >= 0 && ruleIndex < it.size) {
                router.goToActionEditor(currentlyEditedCellIndex, ruleIndex)
            }
        }
    }
}
