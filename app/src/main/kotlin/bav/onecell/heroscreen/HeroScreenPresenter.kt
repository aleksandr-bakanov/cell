package bav.onecell.heroscreen

import bav.onecell.common.router.Router
import bav.onecell.model.GameRules
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class HeroScreenPresenter(
        private val view: HeroScreen.View,
        private val gameRules: GameRules,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : HeroScreen.Presenter {

    companion object {
        private const val TAG = "EditorPresenter"
    }

    private var cell: Cell? = null
    private val cellProvider = BehaviorSubject.create<Cell>()
    private val backgroundFieldRadiusProvider = BehaviorSubject.create<Int>()

    override fun initialize(cellIndex: Int) {
        cellRepository.loadFromStore()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    cell = cellRepository.getCell(cellIndex)
                    backgroundFieldRadiusProvider.onNext(4)
                    cellProvider.onNext(cell!!)
                }
    }

    override fun addHexToCell(hex: Hex) {
        cell?.let {
            if (gameRules.isAllowedToAddHexIntoCell(it, hex) and gameRules.userHasEnoughMoney(it, hex)) {
                it.removeMoney(it.hexTypeToPrice(hex.type))
                it.addHex(hex)
                it.evaluateCellHexesPower()
                view.highlightTips(hex.type)
            }
        }
    }

    override fun removeHexFromCell(hex: Hex) {
        cell?.let {
            if (gameRules.isAllowedToRemoveHexFromCell(it, hex)) {
                it.addMoney(it.hexTypeToPrice(it.data.hexes[hex.hashCode()]?.type ?: Hex.Type.REMOVE))
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

    override fun getTipHexes(type: Hex.Type): Collection<Hex> {
        cell?.let { c ->
            c.updateOutlineHexes()
            return c.getOutlineHexes().filter { hex -> gameRules.isAllowedToAddHexIntoCell(c, hex.withType(type)) }
        }
        return mutableSetOf()
    }

    override fun openMainMenu() {
        router.goToMain()
    }
}
