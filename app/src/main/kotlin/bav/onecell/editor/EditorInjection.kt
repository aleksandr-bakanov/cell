package bav.onecell.editor

import bav.onecell.common.router.Router
import bav.onecell.di.scopes.FragmentScope
import bav.onecell.model.RepositoryContract
import bav.onecell.model.GameRules
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [EditorModule::class])
interface EditorSubcomponent {
    fun inject(view: EditorFragment)
}

@Module
class EditorModule(val view: Editor.View) {

    @Provides
    @FragmentScope
    fun provideEditorPresenter(gameRules: GameRules, cellRepository: RepositoryContract.CellRepo, router: Router):
            Editor.Presenter {
        return EditorPresenter(view, gameRules, cellRepository, router)
    }
}
