package bav.onecell.cellslist

import bav.onecell.common.router.Router
import bav.onecell.di.scopes.FragmentScope
import bav.onecell.model.RepositoryContract
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [CellsListModule::class])
interface CellsListSubcomponent {
    fun inject(view: CellsListFragment)
}

@Module
class CellsListModule() {
    @Provides
    @FragmentScope
    fun provideCellsListPresenter(router: Router, cellRepository: RepositoryContract.CellRepo):
            CellsList.Presenter {
        return CellsListPresenter(cellRepository, router)
    }
}