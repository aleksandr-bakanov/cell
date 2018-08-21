package bav.onecell.battle

import bav.onecell.common.router.Router
import bav.onecell.di.scopes.FragmentScope
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
    fun provideBattlePresenter(battleEngine: BattleEngine, router: Router): Battle.Presenter {
        return BattlePresenter(view, battleEngine, router)
    }
}
