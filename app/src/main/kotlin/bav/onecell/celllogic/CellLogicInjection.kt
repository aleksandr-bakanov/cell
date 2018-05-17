package bav.onecell.celllogic

import bav.onecell.celllogic.conditions.ConditionEditor
import bav.onecell.celllogic.conditions.ConditionEditorDialogFragment
import bav.onecell.celllogic.conditions.ConditionEditorPresenter
import bav.onecell.celllogic.conditions.ConditionListFragment
import bav.onecell.celllogic.conditions.Conditions
import bav.onecell.celllogic.conditions.ConditionsPresenter
import bav.onecell.celllogic.rules.ActionEditorDialogFragment
import bav.onecell.celllogic.rules.RuleListFragment
import bav.onecell.celllogic.rules.Rules
import bav.onecell.celllogic.rules.RulesPresenter
import bav.onecell.common.router.Router
import bav.onecell.di.scopes.FragmentScope
import bav.onecell.model.RepositoryContract
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [CellLogicModule::class])
interface CellLogicSubcomponent {
    fun inject(view: RuleListFragment)
    fun inject(view: ConditionListFragment)
    fun inject(view: ActionEditorDialogFragment)
    fun inject(view: ConditionEditorDialogFragment)
}

@Module
class CellLogicModule {
    @Provides
    @FragmentScope
    fun provideRulesPresenter(cellRepository: RepositoryContract.CellRepo, router: Router): Rules.Presenter {
        return RulesPresenter(cellRepository, router)
    }

    @Provides
    @FragmentScope
    fun provideConditionsPresenter(cellRepository: RepositoryContract.CellRepo, router: Router): Conditions.Presenter {
        return ConditionsPresenter(cellRepository, router)
    }

    @Provides
    @FragmentScope
    fun provideConditionEditorPresenter(): ConditionEditor.Presenter {
        return ConditionEditorPresenter()
    }
}
