package bav.onecell.battle

import bav.onecell.model.BattleInfo
import bav.onecell.model.battle.FrameGraphics
import io.reactivex.Observable
import kotlinx.coroutines.Job

interface Battle {

    interface View {
    }

    interface Presenter {
        /**
         * Presenter initializer
         *
         * @param params Initial battle params in JSON
         */
        fun initialize(params: String)

        fun battleResultsProvider(): Observable<BattleInfo>

        fun stopBattleEvaluation()
    }

    interface FramesFactory {
        /**
         * Returns pairs keys representing time in milliseconds from battle start and values representing frames.
         */
        fun framesProvider(): Observable<Pair<Long, FrameGraphics?>>

        fun progressProvider(): Observable<Int>

        /**
         * Generates frames from provided battle info. Will emit result via [framesProvider].
         */
        fun generateFrames(battleInfo: BattleInfo): Job
    }
}