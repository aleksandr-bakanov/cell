package bav.onecell.battle

import bav.onecell.model.BattleFieldSnapshot
import bav.onecell.model.hexes.Hex
import io.reactivex.Observable

interface Battle {

    interface View {
        fun setBackgroundFieldRadius(radius: Int)
        fun updateBattleView(snapshotIndex: Int = 0)
        fun setRing(ring: List<Hex>)
        fun reportBattleEnd()
        fun setSnapshots(snapshots: List<BattleFieldSnapshot>)
    }

    interface Presenter {
        /**
         * Presenter initializer
         *
         * @param cellIndexes Indexes of cells within cell repository
         */
        fun initialize(cellIndexes: List<Int>)

        /**
         * Initialize next battle step calculation
         */
        fun doFullStep()

        fun doPartialStep()

        fun finishBattle()

        fun snapshotsCounter(): Observable<Int>
    }
}