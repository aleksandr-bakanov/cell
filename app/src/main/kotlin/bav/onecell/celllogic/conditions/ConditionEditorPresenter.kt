package bav.onecell.celllogic.conditions

import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Condition

class ConditionEditorPresenter : ConditionEditor.Presenter {

    private lateinit var condition: Condition
    private var whatToEdit: Int = -1

    override fun initialize(condition: Condition, whatToEdit: Int) {
        this.condition = condition
        this.whatToEdit = whatToEdit
    }

    private val emptyValues: Array<String> = arrayOf()
    private val fieldToCheckValues: Array<String> = arrayOf(
            Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY.toString())
    private val operationsValues: Array<String> = arrayOf(Condition.Operation.EQUALS.toString())
    private val directionValues: Array<String> = arrayOf(
            Cell.Direction.N.toString(),
            Cell.Direction.NE.toString(),
            Cell.Direction.SE.toString(),
            Cell.Direction.S.toString(),
            Cell.Direction.SW.toString(),
            Cell.Direction.NW.toString()
    )

    override fun provideConditionDialogValues(): Array<String> {
        return when (whatToEdit) {
            ConditionsPresenter.ConditionPartToEdit.FIELD.ordinal -> fieldToCheckValues
            ConditionsPresenter.ConditionPartToEdit.OPERATION.ordinal -> operationsValues
            ConditionsPresenter.ConditionPartToEdit.EXPECTED.ordinal -> directionValues // TODO: should depend on fieldToCheck
            else -> emptyValues
        }
    }

    override fun saveConditionValue(which: Int) {
        when (whatToEdit) {
            ConditionsPresenter.ConditionPartToEdit.FIELD.ordinal -> condition.fieldToCheck = Condition.FieldToCheck.fromInt(which)
            ConditionsPresenter.ConditionPartToEdit.OPERATION.ordinal -> condition.operation = Condition.Operation.fromInt(which)
            ConditionsPresenter.ConditionPartToEdit.EXPECTED.ordinal -> condition.expected = which // TODO: should depend on fieldToCheck
            else -> Unit
        }
    }
}
