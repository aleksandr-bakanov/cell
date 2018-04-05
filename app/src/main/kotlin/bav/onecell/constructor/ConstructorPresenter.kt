package bav.onecell.constructor

import android.util.Log
import bav.onecell.common.router.Router
import bav.onecell.model.Cell
import bav.onecell.model.RepositoryContract
import bav.onecell.model.Rules
import bav.onecell.model.hexes.Hex

class ConstructorPresenter(
        private val view: Constructor.View,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : Constructor.Presenter {

    companion object {
        private const val TAG = "ConstructorPresenter"
    }

    private var cell: Cell? = null

    override fun initialize(cellIndex: Int) {
        cell = cellRepository.getCell(cellIndex)
        view.setBackgroundFieldRadius(2)
        view.setCell(cell)
    }

    override fun addHexToCell(hex: Hex) {
        cell?.let {
            if (Rules.instance.isAllowedToAddHexIntoCell(it, hex)) {
                it.addHex(hex)
                it.evaluateCellHexesPower()
            }
        }
    }

    override fun removeHexFromCell(hex: Hex) {
        Log.d(TAG, "removeHexFromCell $hex")
        cell?.let {
            if (Rules.instance.isAllowedToRemoveHexFromCell(it, hex)) {
                it.removeHex(hex)
                it.evaluateCellHexesPower()
            }
        }

    }

    override fun rotateCellLeft() {
        cell?.let {
            it.rotateLeft()
            it.evaluateCellHexesPower()
        }
    }

    override fun rotateCellRight() {
        cell?.let {
            it.rotateRight()
            it.evaluateCellHexesPower()
        }
    }
}