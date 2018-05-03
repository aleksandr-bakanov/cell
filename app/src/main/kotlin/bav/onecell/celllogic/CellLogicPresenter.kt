package bav.onecell.celllogic

import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.logic.Rule
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class CellLogicPresenter(
        private val cellRepository: RepositoryContract.CellRepo) : CellLogic.Presenter {

    companion object {
        private const val TAG = "CellLogicPresenter"
    }

    private var rules: MutableList<Rule>? = null
    private val rulesNotifier = PublishSubject.create<Unit>()

    override fun initialize(cellIndex: Int) {
        rules = cellRepository.getCell(cellIndex)?.data?.rules
    }

    override fun rulesCount(): Int = rules?.size ?: 0

    override fun createNewRule() {
        rules?.let {
            it.add(Rule())
            rulesNotifier.onNext(Unit)
        }
    }

    override fun removeRule(index: Int) {
        rules?.let {
            if (index in 0 until it.size) it.removeAt(index)
            rulesNotifier.onNext(Unit)
        }
    }

    override fun rulesUpdateNotifier(): Observable<Unit> = rulesNotifier
}
