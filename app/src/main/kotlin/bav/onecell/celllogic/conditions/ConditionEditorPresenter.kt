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
        return when (whatToEdit) {
            ConditionsPresenter.ConditionPartToEdit.FIELD.value -> fieldToCheckValues
            ConditionsPresenter.ConditionPartToEdit.OPERATION.value -> operationsValues
            ConditionsPresenter.ConditionPartToEdit.EXPECTED.value -> directionValues // TODO: should depend on fieldToCheck
            else -> emptyValues
        }
    }

    override fun saveConditionValue(which: Int) {
        when (whatToEdit) {
            ConditionsPresenter.ConditionPartToEdit.FIELD.value -> condition.fieldToCheck = Condition.FieldToCheck.fromString(
                    fieldToCheckValues[which])
            ConditionsPresenter.ConditionPartToEdit.OPERATION.value -> condition.operation = Condition.Operation.fromString(
                    operationsValues[which])
            ConditionsPresenter.ConditionPartToEdit.EXPECTED.value -> condition.expected = Cell.Direction.fromString(
                    directionValues[which]) // TODO: should depend on fieldToCheck
            else -> Unit
        }
    }
}