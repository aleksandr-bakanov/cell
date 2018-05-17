package bav.onecell.battle

import bav.onecell.common.router.Router
import bav.onecell.di.scopes.FragmentScope
import bav.onecell.model.GameRules
import bav.onecell.model.RepositoryContract
import bav.onecell.model.hexes.HexMath
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [BattleModule::class])
interface BattleSubcomponent {
    fun inject(view: BattleFragment)
}

@Module
class BattleModule(val view: Battle.View) {
    @Provides
    @FragmentScope
    fun provideBattlePresenter(hexMath: HexMath, gameRules: GameRules, cellRepository: RepositoryContract.CellRepo,
                               router: Router): Battle.Presenter {
        return BattlePresenter(view, hexMath, gameRules, cellRepository, router)
    }
}
