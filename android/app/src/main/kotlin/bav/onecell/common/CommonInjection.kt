package bav.onecell.common

import android.content.Context
import bav.onecell.common.router.Router
import bav.onecell.common.router.RouterImpl
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
}
