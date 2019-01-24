package bav.onecell.cellslist

import bav.onecell.cellslist.cellselection.CellsForBattle
import bav.onecell.cellslist.cellselection.CellsForBattleFragment
import bav.onecell.cellslist.cellselection.CellsForBattlePresenter
import bav.onecell.di.scopes.FragmentScope
import bav.onecell.model.RepositoryContract
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [CellsListModule::class])
interface CellsListSubcomponent {
    fun inject(view: CellsForBattleFragment)
}

@Module
class CellsListModule {
    @Provides
    @FragmentScope
    fun provideCellsForBattlePresenter(cellRepository: RepositoryContract.CellRepo): CellsForBattle.Presenter {
        return CellsForBattlePresenter(cellRepository)
    }
}
