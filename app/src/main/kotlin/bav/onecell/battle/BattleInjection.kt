package bav.onecell.battle

import bav.onecell.di.scopes.ActivityScope
import bav.onecell.model.RepositoryContract
import bav.onecell.model.GameRules
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
    fun provideBattlePresenter(hexMath: HexMath, gameRules: GameRules, cellRepository: RepositoryContract.CellRepo):
            Battle.Presenter {
        return BattlePresenter(view, hexMath, gameRules, cellRepository)
    }
}