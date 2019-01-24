package bav.onecell.main

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
class MainModule {
    @Provides
    @ActivityScope
    fun provideMainPresenter(cellRepo: RepositoryContract.CellRepo): Main.Presenter {
        return MainPresenter(cellRepo)
    }
}
