package bav.onecell.celllogic.conditions

import bav.onecell.common.router.Router
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Condition
import bav.onecell.model.cell.logic.Rule
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ConditionsPresenter(private val cellRepository: RepositoryContract.CellRepo,
                          private val router: Router) : Conditions.Presenter {

    companion object {
        private const val TAG = "CellLogicPresenter"
    }

    enum class ConditionPartToEdit {
        FIELD, OPERATION, EXPECTED
    }

    private val conditionsNotifier = PublishSubject.create<Unit>()

    private var currentlyEditedRule: Rule? = null

    override fun initialize(cellIndex: Int, ruleIndex: Int) {
        currentlyEditedRule = cellRepository.getCell(cellIndex)?.data?.rules?.get(ruleIndex)
    }

    override fun conditionsUpdateNotifier(): Observable<Unit> = conditionsNotifier

    override fun conditionsCount(): Int = currentlyEditedRule?.size() ?: 0

    override fun createNewCondition() {
        currentlyEditedRule?.addCondition(Condition())
        conditionsNotifier.onNext(Unit)
    }

    override fun removeCondition(index: Int) {
        currentlyEditedRule?.removeConditionAt(index)
        conditionsNotifier.onNext(Unit)
    }

    override fun openConditionEditor(conditionIndex: Int, whatToEdit: Int) {
        currentlyEditedRule?.getCondition(conditionIndex)?.let {
            router.goToConditionEditor(it, whatToEdit)
        }
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



}
