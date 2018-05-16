package bav.onecell.main

import bav.onecell.celllogic.CellLogic
import bav.onecell.celllogic.CellLogicActivity
import bav.onecell.celllogic.CellLogicPresenter
import bav.onecell.cellslist.CellsListFragment
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

    @Provides
    @ActivityScope
    fun provideCellLogicPresenter(cellRepository: RepositoryContract.CellRepo): CellLogic.Presenter {
        return CellLogicPresenter(cellRepository)
    }
}
