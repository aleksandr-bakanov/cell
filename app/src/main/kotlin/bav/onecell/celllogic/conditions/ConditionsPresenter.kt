package bav.onecell.celllogic.conditions

import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Condition
import bav.onecell.model.cell.logic.Rule
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ConditionsPresenter(private val cellRepository: RepositoryContract.CellRepo) : Conditions.Presenter {

    companion object {
        private const val TAG = "CellLogicPresenter"
    }

    enum class ConditionPartToEdit(val value: Int) {
        FIELD(0), OPERATION(1), EXPECTED(2)
    }

    private val conditionsNotifier = PublishSubject.create<Unit>()
    private val conditionEditNotifier = PublishSubject.create<Condition>()

    private var currentlyEditedRule: Rule? = null
    private var currentlyEditedConditionIndex: Int = -1
    private var currentlyEditedCondition: Condition? = null
    private var currentlyWhatToEdit: Int = -1

    override fun initialize(cellIndex: Int, ruleIndex: Int) {
        currentlyEditedRule = cellRepository.getCell(cellIndex)?.data?.rules?.get(ruleIndex)
    }

    override fun conditionsUpdateNotifier(): Observable<Unit> = conditionsNotifier

    override fun conditionsEditNotifier(): Observable<Condition> = conditionEditNotifier

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
        currentlyEditedCondition = currentlyEditedRule?.getCondition(conditionIndex)
        currentlyEditedCondition?.let {
            currentlyEditedConditionIndex = conditionIndex
            currentlyWhatToEdit = whatToEdit
            conditionEditNotifier.onNext(it)
        }
    }

    private val emptyValues: Array<String> = arrayOf()
    private val fieldToCheckValues: Array<String> = arrayOf(Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY.value)
    private val operationsValues: Array<String> = arrayOf(Condition.Operation.EQUALS.value)
    private val directionValues: Array<String> = arrayOf(
            Cell.Direction.N.toString(),
            Cell.Direction.NE.toString(),
            Cell.Direction.SE.toString(),
            Cell.Direction.S.toString(),
            Cell.Direction.SW.toString(),
            Cell.Direction.NW.toString()
    )

    override fun provideConditionDialogValues(): Array<String> {
        return when (currentlyWhatToEdit) {
            ConditionPartToEdit.FIELD.value -> fieldToCheckValues
            ConditionPartToEdit.OPERATION.value -> operationsValues
            ConditionPartToEdit.EXPECTED.value -> directionValues // TODO: should depend on fieldToCheck
            else -> emptyValues
        }
    }

    override fun provideActionDialogValues(): Array<String> {
        return directionValues
    }

    override fun saveConditionValue(which: Int) {
        currentlyEditedCondition?.let {
            when (currentlyWhatToEdit) {
                ConditionPartToEdit.FIELD.value -> it.fieldToCheck = Condition.FieldToCheck.fromString(
                        fieldToCheckValues[which])
                ConditionPartToEdit.OPERATION.value -> it.operation = Condition.Operation.fromString(
                        operationsValues[which])
                ConditionPartToEdit.EXPECTED.value -> it.expected = Cell.Direction.fromString(
                        directionValues[which]) // TODO: should depend on fieldToCheck
                else -> Unit
            }
        }
    }

}
