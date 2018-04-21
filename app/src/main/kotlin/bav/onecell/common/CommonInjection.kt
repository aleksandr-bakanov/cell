package bav.onecell.common

import android.content.Context
import bav.onecell.common.router.Router
import bav.onecell.common.router.RouterImpl
import bav.onecell.common.storage.Storage
import bav.onecell.common.storage.StorageImpl
import bav.onecell.model.Rules
import bav.onecell.model.hexes.HexMath
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class CommonModule {
    @Provides
    @Singleton
    fun provideRouter(@Named("app_context") context: Context): Router {
        return RouterImpl(context)
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
