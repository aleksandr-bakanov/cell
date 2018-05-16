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
    fun inject(view: MainFragment)
    fun inject(view: MainActivity)
}

@Module
class MainModule {

    @Provides
    @ActivityScope
    fun provideMainPresenter(router: Router): Main.Presenter {
        return MainPresenter(router)
    }
}
