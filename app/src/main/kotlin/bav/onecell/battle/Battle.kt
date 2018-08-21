package bav.onecell.battle

import bav.onecell.model.BattleInfo
import io.reactivex.Observable

interface Battle {

    interface View {
        fun updateBattleView(snapshotIndex: Int = 0)
    }

    interface Presenter {
        /**
         * Presenter initializer
         *
         * @param cellIndexes Indexes of cells within cell repository
         */
        fun initialize(cellIndexes: List<Int>)

        fun finishBattle()

        fun battleResultsProvider(): Observable<BattleInfo>
    }
}