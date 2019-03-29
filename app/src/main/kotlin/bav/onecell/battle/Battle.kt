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
        fun generateFrameGraphics(battleInfo: BattleInfo, timestamp: Long, out: FrameGraphics)
    }
}