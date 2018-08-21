package bav.onecell.battle

import bav.onecell.common.router.Router
import bav.onecell.model.BattleInfo
import io.reactivex.subjects.PublishSubject

class BattlePresenter(
        private val view: Battle.View,
        private val battleEngine: BattleEngine,
        private val router: Router) : Battle.Presenter {

    companion object {
        private const val TAG = "BattlePresenter"
    }

    //region Overridden methods
    override fun battleResultsProvider(): PublishSubject<BattleInfo> = battleEngine.battleResultProvider

    override fun initialize(cellIndexes: List<Int>) {
        view.updateBattleView()
        battleEngine.initialize(cellIndexes)
    }

    override fun finishBattle() {
        router.goBack()
    }
    //endregion
}
