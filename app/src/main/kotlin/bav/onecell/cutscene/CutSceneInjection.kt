package bav.onecell.cutscene

import bav.onecell.di.scopes.FragmentScope
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [CutSceneModule::class])
interface CutSceneSubcomponent {
    fun inject(view: CutSceneFragment)
}

@Module
class CutSceneModule {
    @Provides
    @FragmentScope
    fun provideCutScenePresenter(): CutScene.Presenter {
        return CutScenePresenter()
    }
}
