package bav.onecell.common

import bav.onecell.common.router.Router
import bav.onecell.common.router.RouterImpl
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
}
