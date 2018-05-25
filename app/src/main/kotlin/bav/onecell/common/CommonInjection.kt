package bav.onecell.common

import android.content.Context
import bav.onecell.common.router.Router
import bav.onecell.common.router.RouterImpl
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.GameRules
import bav.onecell.model.hexes.HexMath
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class CommonModule {
    @Provides
    @Singleton
    fun provideRouter(): Router {
        return RouterImpl()
    }

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
}
