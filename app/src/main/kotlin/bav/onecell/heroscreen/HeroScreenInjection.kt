package bav.onecell.heroscreen

import bav.onecell.common.router.Router
import bav.onecell.di.scopes.FragmentScope
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
    fun provideHeroScreenPresenter(router: Router): HeroScreen.Presenter {
        return HeroScreenPresenter(router)
    }
}
