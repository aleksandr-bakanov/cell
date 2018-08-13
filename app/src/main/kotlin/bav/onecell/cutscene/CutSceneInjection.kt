package bav.onecell.cutscene

import bav.onecell.common.router.SceneManager
import bav.onecell.di.scopes.ActivityScope
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [CutSceneModule::class])
interface CutSceneSubcomponent {
    fun inject(view: CutSceneFragment)
}

@Module
class CutSceneModule {
    @Provides
    @ActivityScope
    fun provideCutScenePresenter(sceneManager: SceneManager): CutScene.Presenter {
        return CutScenePresenter(sceneManager)
    }
}
