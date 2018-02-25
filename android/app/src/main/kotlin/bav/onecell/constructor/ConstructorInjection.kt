package bav.onecell.constructor

import bav.onecell.common.router.Router
import bav.onecell.di.scopes.ActivityScope
import bav.onecell.model.CellRepository
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [ConstructorModule::class])
interface ConstructorSubcomponent {
    fun inject(view: ConstructorActivity)
}

@Module
class ConstructorModule(val view: Constructor.View) {
    @Provides
    @ActivityScope
    fun provideConstructorPresenter(cellRepository: CellRepository, router: Router): Constructor.Presenter {
        return ConstructorPresenter(view, cellRepository, router)
    }
}
