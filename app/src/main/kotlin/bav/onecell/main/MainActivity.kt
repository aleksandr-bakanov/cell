package bav.onecell.main

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.battle.BattleFragment
import bav.onecell.celllogic.conditions.ConditionListFragment
import bav.onecell.celllogic.rules.RuleListFragment
import bav.onecell.cellslist.CellsListFragment
import bav.onecell.cellslist.cellselection.CellsForBattleFragment
import bav.onecell.common.router.Router
import bav.onecell.common.router.Router.WindowType.BATTLE
import bav.onecell.common.router.Router.WindowType.BATTLE_CELLS_SELECTION
import bav.onecell.common.router.Router.WindowType.CELLS_LIST
import bav.onecell.common.router.Router.WindowType.CELL_EDITOR
import bav.onecell.common.router.Router.WindowType.CONDITIONS_EDITOR
import bav.onecell.common.router.Router.WindowType.MAIN
import bav.onecell.common.router.Router.WindowType.RULES_EDITOR
import bav.onecell.editor.EditorFragment
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MainActivity : FragmentActivity() {

    @Inject
    lateinit var router: Router
    private val disposables: CompositeDisposable = CompositeDisposable()

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject()
        router.setHostActivity(this)

        setContentView(R.layout.activity_main)
        changeWindow(Router.Window(MAIN))

        disposables.add(router.windowChange().subscribe { changeWindow(it) })
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
    //endregion

    //region Private methods
    private fun inject() {
        (application as OneCellApplication).appComponent
                .plus(MainModule())
                .inject(this)
    }

    private fun changeWindow(window: Router.Window) {
        val fragment = when (window.type) {
            MAIN -> MainFragment.newInstance()
            CELLS_LIST -> CellsListFragment.newInstance()
            BATTLE_CELLS_SELECTION -> CellsForBattleFragment.newInstance()
            BATTLE -> BattleFragment.newInstance(window.args)
            CELL_EDITOR -> EditorFragment.newInstance(window.args)
            RULES_EDITOR -> RuleListFragment.newInstance(window.args)
            CONDITIONS_EDITOR -> ConditionListFragment.newInstance(window.args)
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.holder, fragment).addToBackStack(window.toString())
        ft.commit()
    }
    //endregion
}
