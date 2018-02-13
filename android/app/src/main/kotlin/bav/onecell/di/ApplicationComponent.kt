package bav.onecell.di

import bav.onecell.common.CommonModule
import bav.onecell.main.MainModule
import bav.onecell.main.MainSubcomponent
import bav.onecell.model.ModelModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ModelModule::class, CommonModule::class])
interface ApplicationComponent {
    fun plus(mainModule: MainModule): MainSubcomponent
}