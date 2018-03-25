package bav.onecell.di

import android.content.Context
import bav.onecell.battle.BattleModule
import bav.onecell.battle.BattleSubcomponent
import bav.onecell.common.CommonModule
import bav.onecell.constructor.ConstructorModule
import bav.onecell.constructor.ConstructorSubcomponent
import bav.onecell.main.MainModule
import bav.onecell.main.MainSubcomponent
import bav.onecell.model.ModelModule
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ModelModule::class, CommonModule::class])
interface AppComponent {
    fun plus(mainModule: MainModule): MainSubcomponent
    fun plus(constructorModule: ConstructorModule): ConstructorSubcomponent
    fun plus(battleModule: BattleModule): BattleSubcomponent
}

@Module
class AppModule(private val context: Context) {
    @Provides
    @Singleton
    @Named("app_context")
    fun provideContext(): Context = context
}