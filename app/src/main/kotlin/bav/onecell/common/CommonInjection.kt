package bav.onecell.common

import android.content.Context
import bav.onecell.battle.Battle
import bav.onecell.battle.BattleEngine
import bav.onecell.battle.BattleGraphics
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.GameRules
import bav.onecell.model.RepositoryContract
import bav.onecell.model.hexes.HexMath
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class CommonModule {
    @Provides
    @Singleton
    fun provideHexMath(): HexMath {
        return HexMath()
    }

    @Provides
    @Singleton
    fun provideRules(hexMath: HexMath): GameRules {
        return GameRules(hexMath)
    }

    @Provides
    @Singleton
    fun provideDrawUtils(hexMath: HexMath, @Named("app_context") context: Context): DrawUtils {
        return DrawUtils(hexMath, context)
    }

    @Provides
    @Singleton
    fun provideBattleEngine(hexMath: HexMath, gameRules: GameRules, cellRepository: RepositoryContract.CellRepo): BattleEngine {
        return BattleEngine(hexMath, gameRules, cellRepository)
    }

    @Provides
    @Singleton
    fun provideBattleGraphicsFactory(drawUtils: DrawUtils, hexMath: HexMath): Battle.FramesFactory {
        return BattleGraphics(drawUtils, hexMath)
    }

    @Provides
    @Singleton
    fun provideResourceProvider(@Named("app_context") context: Context): Common.ResourceProvider {
        return ResourceProviderImpl(context)
    }

    @Provides
    @Singleton
    fun provideGameState(@Named("app_context") context: Context): Common.GameState {
        return GameStateImpl(context)
    }

    @Provides
    @Singleton
    fun provideAnalytics(@Named("app_context") context: Context): Common.Analytics {
        return AnalyticsImpl(context)
    }
}
