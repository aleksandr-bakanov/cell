package bav.onecell.battle

import android.util.Log
import bav.onecell.common.router.Router
import bav.onecell.model.BattleInfo
import bav.onecell.model.InitialBattleParams
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

    override fun initialize(params: String) {
        view.drawSnapshotInitialState()
        Log.d(TAG, "params = $params")
        battleEngine.initialize(InitialBattleParams.fromJson(params))
    }

    override fun finishBattle(battleInfo: BattleInfo) {
        router.goToBattleResults(battleInfo.damageDealtByCells, battleInfo.deadOrAliveCells)
    }
    //endregion
}
