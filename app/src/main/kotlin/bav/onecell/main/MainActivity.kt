package bav.onecell.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.battle.BattleFragment
import bav.onecell.battle.results.BattleResultsFragment
import bav.onecell.cellslist.cellselection.CellsForBattleFragment
import bav.onecell.common.Common
import bav.onecell.common.router.Router
import bav.onecell.common.router.Router.WindowType.BATTLE
import bav.onecell.common.router.Router.WindowType.BATTLE_CELLS_SELECTION
import bav.onecell.common.router.Router.WindowType.BATTLE_RESULTS
import bav.onecell.common.router.Router.WindowType.CUT_SCENE
import bav.onecell.common.router.Router.WindowType.HERO_SCREEN
import bav.onecell.common.router.Router.WindowType.MAIN
import bav.onecell.cutscene.CutSceneFragment
import bav.onecell.heroscreen.HeroScreenFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class MainActivity : FragmentActivity(), Main.NavigationInfoProvider {

    @Inject lateinit var router: Router
    @Inject lateinit var gameState: Common.GameState
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val lastNavDestinationProvider: BehaviorSubject<Int> = BehaviorSubject.create()

    companion object {
        private const val TAG = "MainActivity"
    }

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject()
        router.setHostActivity(this)

        setContentView(R.layout.activity_main)

        disposables.add(router.windowChange().subscribe { changeWindow(it) })

        lastNavDestinationProvider.onNext(gameState.getLastNavDestinationId())
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
    //endregion

    //region Overridden methods
    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.currentDestination?.let {
            if (it.id != R.id.mainFragment) {
                gameState.setLastNavDestinationId(it.id, true)
                lastNavDestinationProvider.onNext(it.id)
                navController.popBackStack(R.id.mainFragment, false)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUi()
    }

    override fun provideLastDestination(): Observable<Int> = lastNavDestinationProvider
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
            BATTLE_CELLS_SELECTION -> CellsForBattleFragment.newInstance()
            BATTLE -> BattleFragment.newInstance(window.args)
            CUT_SCENE -> CutSceneFragment.newInstance(window.args)
            BATTLE_RESULTS -> BattleResultsFragment.newInstance(window.args)
            HERO_SCREEN -> HeroScreenFragment.newInstance(window.args)
            else -> null
        }
        /*supportFragmentManager.beginTransaction()
                .replace(R.id.holder, fragment)
                //.addToBackStack(null)
                .commit()*/
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
