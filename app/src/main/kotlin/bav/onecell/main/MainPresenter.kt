package bav.onecell.main

import bav.onecell.common.router.Router
import bav.onecell.model.RepositoryContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainPresenter(
        private val view: Main.View,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : Main.Presenter {

    companion object {
        private const val TAG = "MainPresenter"
    }

    //region Overridden methods
    override fun createNewCell() {
        cellRepository.createNewCell()
        view.notifyCellRepoListUpdated()
    }

    override fun cellsCount(): Int = cellRepository.cellsCount()

    override fun openCellConstructor(cellIndex: Int) {
        router.goToCellConstructor(cellIndex)
    }

    override fun openBattleView(cellIndexes: List<Int>) {
        router.goToBattleView(cellIndexes)
    }

    override fun removeCell(cellIndex: Int) {
        cellRepository.removeCell(cellIndex)
        view.notifyCellRepoListUpdated()
    }

    override fun initialize() {
        cellRepository.loadFromStore()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {}, {
                    view.notifyCellRepoListUpdated()
                })
    }
    //endregion

    //region Lifecycle events
    override fun onPause() {
        cellRepository.storeCells()
    }
    //endregion
}