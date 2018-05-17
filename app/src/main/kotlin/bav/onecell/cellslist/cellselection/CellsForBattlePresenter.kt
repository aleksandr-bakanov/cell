package bav.onecell.cellslist.cellselection

import bav.onecell.common.router.Router
import bav.onecell.model.RepositoryContract
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class CellsForBattlePresenter(
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : CellsForBattle.Presenter {

    companion object {
        private const val TAG = "CellsForBattlePresenter"
    }

    private val cellRepoNotifier = PublishSubject.create<Unit>()

    override fun cellsCount(): Int = cellRepository.cellsCount()

    override fun initialize() {
        cellRepository.loadFromStore()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    cellRepoNotifier.onNext(Unit)
                })
    }

    override fun cellRepoUpdateNotifier(): Observable<Unit> = cellRepoNotifier

    override fun startBattle(cellIndexes: List<Int>) {
        router.goToBattle(cellIndexes)
    }
    //endregion
}
