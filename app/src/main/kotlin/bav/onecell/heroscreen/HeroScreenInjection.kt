package bav.onecell.heroscreen

import bav.onecell.common.Common
import bav.onecell.common.router.Router
import bav.onecell.di.scopes.FragmentScope
import bav.onecell.model.GameRules
import bav.onecell.model.RepositoryContract
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [HeroScreenModule::class])
interface HeroScreenSubcomponent {
    fun inject(view: HeroScreenFragment)
}

@Module
class HeroScreenModule(val view: HeroScreen.View) {
    @Provides
    @FragmentScope
    fun provideHeroScreenPresenter(gameRules: GameRules, gameState: Common.GameState,
                                   cellRepo: RepositoryContract.CellRepo, router: Router,
                                   resourceProvider: Common.ResourceProvider) : HeroScreen.Presenter {
        return HeroScreenPresenter(view, gameRules, gameState, cellRepo, router, resourceProvider)
    }
}
