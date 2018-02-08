package bav.onecell.main

import bav.onecell.di.scopes.ActivityScope
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
    fun providesMainPresenter(): Main.Presenter {
        return MainPresenter(view)
    }

}

