package bav.onecell.battle

import bav.onecell.model.BattleInfo
import bav.onecell.model.battle.FrameGraphics
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

    interface FramesFactory {
        /**
         * Returns map of frames with keys representing time in milliseconds from battle start.
         */
        fun framesProvider(): Observable<Map<Long, FrameGraphics>>

        /**
         * Generates frames from provided battle info. Will emit result via [framesProvider].
         */
        fun generateFrames(battleInfo: BattleInfo)
    }
}