package bav.onecell.main

import bav.onecell.common.Common
import bav.onecell.di.scopes.ActivityScope
import bav.onecell.model.RepositoryContract
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [MainModule::class])
interface MainSubcomponent {
    fun inject(view: MainFragment)
    fun inject(view: MainActivity)
    fun inject(view: NewGameFragment)
}

@Module
class MainModule(private val view: Main.View?) {
    @Provides
    @ActivityScope
    fun provideMainPresenter(gameState: Common.GameState, analytics: Common.Analytics, cellRepo: RepositoryContract.CellRepo): Main.Presenter {
        return MainPresenter(view, gameState, analytics, cellRepo)
    }
}
