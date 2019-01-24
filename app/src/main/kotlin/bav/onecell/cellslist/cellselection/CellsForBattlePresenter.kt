package bav.onecell.cellslist.cellselection

import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class CellsForBattlePresenter(private val cellRepository: RepositoryContract.CellRepo) : CellsForBattle.Presenter {

    companion object {
        private const val TAG = "CellsForBattlePresenter"
    }

    private val cellRepoNotifier = PublishSubject.create<Unit>()
    private val selectedCells: MutableSet<Int> = mutableSetOf()

    override fun cellsCount(): Int = cellRepository.cellsCount()

    override fun getCell(index: Int): Cell? = cellRepository.getCell(index)

    override fun initialize() {
        cellRepository.loadFromStore()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    cellRepoNotifier.onNext(Unit)
                })
    }

    override fun cellRepoUpdateNotifier(): Observable<Unit> = cellRepoNotifier

    override fun cellSelected(index: Int, selected: Boolean) {
        if (selected) selectedCells.add(index)
        else selectedCells.remove(index)
    }

    override fun isCellSelected(index: Int): Boolean = selectedCells.contains(index)

    override fun getSelectedCells(): List<Int> = selectedCells.toList()
    //endregion
}
