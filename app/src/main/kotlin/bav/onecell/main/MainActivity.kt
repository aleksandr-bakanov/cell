package bav.onecell.main

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.battle.BattleFragment
import bav.onecell.battle.results.BattleResultsFragment
import bav.onecell.celllogic.conditions.ConditionListFragment
import bav.onecell.celllogic.rules.RuleListFragment
import bav.onecell.cellslist.CellsListFragment
import bav.onecell.cellslist.cellselection.CellsForBattleFragment
import bav.onecell.common.router.Router
import bav.onecell.common.router.Router.WindowType.BATTLE
import bav.onecell.common.router.Router.WindowType.BATTLE_CELLS_SELECTION
import bav.onecell.common.router.Router.WindowType.BATTLE_RESULTS
import bav.onecell.common.router.Router.WindowType.CELLS_LIST
import bav.onecell.common.router.Router.WindowType.CELL_EDITOR
import bav.onecell.common.router.Router.WindowType.CONDITIONS_EDITOR
import bav.onecell.common.router.Router.WindowType.CUT_SCENE
import bav.onecell.common.router.Router.WindowType.MAIN
import bav.onecell.common.router.Router.WindowType.RULES_EDITOR
import bav.onecell.cutscene.CutSceneFragment
import bav.onecell.editor.EditorFragment
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MainActivity : FragmentActivity() {

    @Inject lateinit var router: Router
    private val disposables: CompositeDisposable = CompositeDisposable()

    companion object {
        private const val TAG = "MainActivity"
    }

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject()
        router.setHostActivity(this)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.holder, MainFragment.newInstance())
                    .commit()
        }

        disposables.add(router.windowChange().subscribe { changeWindow(it) })

        hideSystemUi()
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
            CUT_SCENE -> CutSceneFragment.newInstance(window.args)
            BATTLE_RESULTS -> BattleResultsFragment.newInstance(window.args)
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.holder, fragment)
                //.addToBackStack(null)
                .commit()
    }

    private fun hideSystemUi() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN
    }
    //endregion
}
