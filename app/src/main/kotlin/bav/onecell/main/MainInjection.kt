package bav.onecell.main

import bav.onecell.celllogic.CellLogic
import bav.onecell.celllogic.CellLogicActivity
import bav.onecell.celllogic.CellLogicPresenter
import bav.onecell.common.router.Router
import bav.onecell.di.scopes.ActivityScope
import bav.onecell.editor.Editor
import bav.onecell.editor.EditorActivity
import bav.onecell.editor.EditorPresenter
import bav.onecell.model.RepositoryContract
import bav.onecell.model.Rules
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [MainModule::class])
interface MainSubcomponent {
    fun inject(view: MainActivity)
    fun inject(view: EditorActivity)
    fun inject(view: CellLogicActivity)
}

@Module
class MainModule(val view: Main.View?) {
    @Provides
    @ActivityScope
    fun provideMainPresenter(cellRepository: RepositoryContract.CellRepo, router: Router): Main.Presenter {
        return MainPresenter(view!!, cellRepository, router)
    }

    @Provides
    @ActivityScope
    fun provideEditorPresenter(rules: Rules, cellRepository: RepositoryContract.CellRepo, router: Router):
            Editor.Presenter {
        return EditorPresenter(rules, cellRepository, router)
    }

    @Provides
    @ActivityScope
    fun provideCellLogicPresenter(cellRepository: RepositoryContract.CellRepo): CellLogic.Presenter {
        return CellLogicPresenter(cellRepository)
    }
}

