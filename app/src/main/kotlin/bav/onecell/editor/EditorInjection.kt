package bav.onecell.editor

import bav.onecell.common.router.Router
import bav.onecell.di.scopes.ActivityScope
import bav.onecell.model.RepositoryContract
import bav.onecell.model.Rules
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [ConstructorModule::class])
interface ConstructorSubcomponent {
    fun inject(view: EditorActivity)
}

@Module
class ConstructorModule(val view: Editor.View) {
    @Provides
    @ActivityScope
    fun provideConstructorPresenter(rules: Rules, cellRepository: RepositoryContract.CellRepo, router: Router):
            Editor.Presenter {
        return EditorPresenter(view, rules, cellRepository, router)
    }
}
