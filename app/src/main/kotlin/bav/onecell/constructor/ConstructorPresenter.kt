package bav.onecell.constructor

import bav.onecell.common.router.Router
import bav.onecell.model.Cell
import bav.onecell.model.RepositoryContract
import bav.onecell.model.Rules
import bav.onecell.model.hexes.Hex

class ConstructorPresenter(
        private val view: Constructor.View,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : Constructor.Presenter {

    private var cell: Cell? = null

    override fun initialize(cellIndex: Int) {
        cell = cellRepository.getCell(cellIndex)
        view.setBackgroundFieldRadius(3)
        view.setCell(cell)
    }

    override fun addHexToCell(hex: Hex) {
        if (Rules.isAllowedToAddHexIntoCell(cell!!, hex)) cell?.hexes?.add(hex)
    }

    override fun removeHexFromCell(hex: Hex) {
        val hexesWithoutHex = cell?.hexes?.filter { it != hex }
        if (Rules.isHexesConnected(hexesWithoutHex!!)) cell?.hexes?.remove(hex)
    }
}