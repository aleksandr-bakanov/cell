package bav.onecell.editor

import bav.onecell.common.router.Router
import bav.onecell.model.cell.Cell
import bav.onecell.model.RepositoryContract
import bav.onecell.model.Rules
import bav.onecell.model.hexes.Hex
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class EditorPresenter(
        private val rules: Rules,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : Editor.Presenter {

    companion object {
        private const val TAG = "EditorPresenter"
    }

    private var cell: Cell? = null
    private val cellProvider = BehaviorSubject.create<Cell>()
    private val backgroundFieldRadiusProvider = BehaviorSubject.create<Int>()

    override fun initialize(cellIndex: Int) {
        cell = cellRepository.getCell(cellIndex)
        backgroundFieldRadiusProvider.onNext(5)
        cellProvider.onNext(cell!!)
    }

    override fun addHexToCell(hex: Hex) {
        cell?.let {
            if (rules.isAllowedToAddHexIntoCell(it, hex)) {
                it.addHex(hex)
                it.evaluateCellHexesPower()
            }
        }
    }

    override fun removeHexFromCell(hex: Hex) {
        cell?.let {
            if (rules.isAllowedToRemoveHexFromCell(it, hex)) {
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

    override fun getCellProvider(): Observable<Cell> = cellProvider
    override fun getBackgroundCellRadiusProvider(): Observable<Int> = backgroundFieldRadiusProvider
}