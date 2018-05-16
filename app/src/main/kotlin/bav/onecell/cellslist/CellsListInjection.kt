package bav.onecell.cellslist

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
class CellsListModule(val view: CellsList.View) {
    @Provides
    @FragmentScope
    fun provideCellsListPresenter(cellRepository: RepositoryContract.CellRepo):
            CellsList.Presenter {
        return CellsListPresenter(view, cellRepository)
    }
}