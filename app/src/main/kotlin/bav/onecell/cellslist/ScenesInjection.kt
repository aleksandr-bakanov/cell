package bav.onecell.cellslist

import bav.onecell.cellslist.cellselection.ScenesFragment
import bav.onecell.di.scopes.FragmentScope
import dagger.Module
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [ScenesModule::class])
interface ScenesSubcomponent {
    fun inject(view: ScenesFragment)
}

@Module
class ScenesModule {

}
