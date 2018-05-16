package bav.onecell.cellslist

import bav.onecell.model.RepositoryContract
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class CellsListPresenter(
        private val view: CellsList.View,
        private val cellRepository: RepositoryContract.CellRepo) : CellsList.Presenter {

    companion object {
        private const val TAG = "CellsListPresenter"
    }

    private val cellRepoNotifier = PublishSubject.create<Unit>()

    //region Overridden methods
    override fun createNewCell() {
        cellRepository.createNewCell()
        cellRepoNotifier.onNext(Unit)
    }

    override fun cellsCount(): Int = cellRepository.cellsCount()

    override fun openCellEditor(cellIndex: Int) {
        view.openCellEditorView(cellIndex)
    }

    override fun openCellRulesEditor(cellIndex: Int) {

    }

    override fun removeCell(cellIndex: Int) {
        cellRepository.removeCell(cellIndex)
        cellRepoNotifier.onNext(Unit)
    }

    override fun initialize() {
        cellRepository.loadFromStore()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {}, {
                    cellRepoNotifier.onNext(Unit)
                })
    }

    override fun cellRepoUpdateNotifier(): Observable<Unit> = cellRepoNotifier
    //endregion

    //region Lifecycle events
    override fun onPause() {
        cellRepository.storeCells()
    }
    //endregion
}