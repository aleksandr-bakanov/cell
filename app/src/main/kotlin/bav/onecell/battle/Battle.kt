package bav.onecell.battle

import bav.onecell.model.BattleInfo
import io.reactivex.Observable

interface Battle {

    interface View {
        fun drawSnapshotInitialState(snapshotIndex: Int = 0)
    }

    interface Presenter {
        /**
         * Presenter initializer
         *
         * @param params Initial battle params in JSON
         */
        fun initialize(params: String)

        fun battleResultsProvider(): Observable<BattleInfo>
    }
}