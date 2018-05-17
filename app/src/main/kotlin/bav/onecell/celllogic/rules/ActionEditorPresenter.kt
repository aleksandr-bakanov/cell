package bav.onecell.celllogic.rules

import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Rule

class ActionEditorPresenter(private val cellRepository: RepositoryContract.CellRepo) : ActionEditor.Presenter {

    private var rule: Rule? = null

    override fun initialize(cellIndex: Int, ruleIndex: Int) {
        rule = cellRepository.getCell(cellIndex)?.data?.rules?.get(ruleIndex)
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
        rule?.action?.value = directionValues[which]
    }
}