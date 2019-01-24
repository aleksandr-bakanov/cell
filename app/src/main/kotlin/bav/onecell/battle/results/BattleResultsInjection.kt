package bav.onecell.battle.results

import bav.onecell.common.Common
import bav.onecell.di.scopes.FragmentScope
import bav.onecell.model.RepositoryContract
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [BattleResultsModule::class])
interface BattleResultsSubcomponent {
    fun inject(view: BattleResultsFragment)
}

@Module
class BattleResultsModule(val view: BattleResults.View) {
    @Provides
    @FragmentScope
    fun provideBattleResultsPresenter(cellRepo: RepositoryContract.CellRepo,
                                      resourceProvider: Common.ResourceProvider, gameState: Common.GameState)
            : BattleResults.Presenter {
        return BattleResultsPresenter(view, cellRepo, resourceProvider, gameState)
    }
}
