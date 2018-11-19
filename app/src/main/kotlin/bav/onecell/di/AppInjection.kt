package bav.onecell.di

import android.content.Context
import bav.onecell.battle.BattleModule
import bav.onecell.battle.BattleSubcomponent
import bav.onecell.battle.results.BattleResultsModule
import bav.onecell.battle.results.BattleResultsSubcomponent
import bav.onecell.cellslist.CellsListModule
import bav.onecell.cellslist.CellsListSubcomponent
import bav.onecell.common.CommonModule
import bav.onecell.common.storage.StorageModule
import bav.onecell.cutscene.CutSceneModule
import bav.onecell.cutscene.CutSceneSubcomponent
import bav.onecell.heroscreen.HeroScreenModule
import bav.onecell.heroscreen.HeroScreenSubcomponent
import bav.onecell.main.MainModule
import bav.onecell.main.MainSubcomponent
import bav.onecell.model.ModelModule
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ModelModule::class, CommonModule::class, StorageModule::class])
interface AppComponent {
    fun plus(mainModule: MainModule): MainSubcomponent
    fun plus(battleModule: BattleModule): BattleSubcomponent
    fun plus(cellsListModule: CellsListModule): CellsListSubcomponent
    fun plus(cutSceneModule: CutSceneModule): CutSceneSubcomponent
    fun plus(battleResultsModule: BattleResultsModule): BattleResultsSubcomponent
    fun plus(heroScreenModule: HeroScreenModule): HeroScreenSubcomponent
}

@Module
class AppModule(private val context: Context) {
    @Provides
    @Singleton
    @Named("app_context")
    fun provideContext(): Context = context
}