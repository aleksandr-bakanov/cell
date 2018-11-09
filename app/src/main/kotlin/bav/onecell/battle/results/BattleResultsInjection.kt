package bav.onecell.battle.results

import bav.onecell.common.Common
import bav.onecell.common.router.Router
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
    fun provideBattleResultsPresenter(router: Router, cellRepo: RepositoryContract.CellRepo,
                                      resourceProvider: Common.ResourceProvider): BattleResults.Presenter {
        return BattleResultsPresenter(view, router, cellRepo, resourceProvider)
    }
}
