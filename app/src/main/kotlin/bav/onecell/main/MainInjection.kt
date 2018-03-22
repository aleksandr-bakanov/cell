package bav.onecell.main

import bav.onecell.common.router.Router
import bav.onecell.di.scopes.ActivityScope
import bav.onecell.model.RepositoryContract
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [MainModule::class])
interface MainSubcomponent {
    fun inject(view: MainActivity)
}

@Module
class MainModule(val view: Main.View) {
    @Provides
    @ActivityScope
    fun provideMainPresenter(cellRepository: RepositoryContract.CellRepo, router: Router): Main.Presenter {
        return MainPresenter(view, cellRepository, router)
    }
}

