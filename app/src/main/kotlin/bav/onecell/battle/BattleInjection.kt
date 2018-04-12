package bav.onecell.battle

import bav.onecell.common.router.Router
import bav.onecell.di.scopes.ActivityScope
import bav.onecell.model.RepositoryContract
import bav.onecell.model.Rules
import bav.onecell.model.hexes.HexMath
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [BattleModule::class])
interface BattleSubcomponent {
    fun inject(view: BattleActivity)
}

@Module
class BattleModule(val view: Battle.View) {
    @Provides
    @ActivityScope
    fun provideConstructorPresenter(hexMath: HexMath, rules: Rules, cellRepository: RepositoryContract.CellRepo, router: Router):
            Battle.Presenter {
        return BattlePresenter(view, hexMath, rules, cellRepository, router)
    }
}