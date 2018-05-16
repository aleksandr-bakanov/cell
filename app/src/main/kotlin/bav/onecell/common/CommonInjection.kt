package bav.onecell.common

import bav.onecell.common.router.Router
import bav.onecell.common.router.RouterImpl
import bav.onecell.model.Rules
import bav.onecell.model.hexes.HexMath
import dagger.Module
import dagger.Provides
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
    fun provideRules(hexMath: HexMath): Rules {
        return Rules(hexMath)
    }


}
