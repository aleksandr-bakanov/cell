package bav.onecell.common.router

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import bav.onecell.battle.BattleFragment
import bav.onecell.battle.results.BattleResultsFragment
import bav.onecell.cutscene.CutSceneFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RouterImpl : Router {

    private lateinit var host: FragmentActivity
    private val windowChanger = PublishSubject.create<Router.Window>()

    override fun windowChange(): Observable<Router.Window> = windowChanger

    override fun goBack() {
        host.supportFragmentManager.popBackStack()
    }

    override fun goToMain() {
        windowChanger.onNext(Router.Window(Router.WindowType.MAIN))
    }

    override fun goToCellsForBattleSelection() {
        windowChanger.onNext(Router.Window(Router.WindowType.BATTLE_CELLS_SELECTION))
    }

    override fun goToBattle(cellIndexes: List<Int>) {
        val bundle = Bundle()
        bundle.putIntegerArrayList(BattleFragment.EXTRA_CELL_INDEXES, ArrayList(cellIndexes))
        windowChanger.onNext(Router.Window(Router.WindowType.BATTLE, bundle))
    }

    override fun setHostActivity(activity: androidx.fragment.app.FragmentActivity) {
        host = activity
    }

    override fun goToCutScene(cutSceneInfo: String) {
        val bundle = Bundle()
        bundle.putString(CutSceneFragment.CUT_SCENE_INFO, cutSceneInfo)
        windowChanger.onNext(Router.Window(Router.WindowType.CUT_SCENE, bundle))
    }

    override fun goToBattleResults(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>) {
        val bundle = Bundle()
        bundle.putIntArray(BattleResultsFragment.CELL_INDEXES, dealtDamage.keys.toIntArray())
        bundle.putIntArray(BattleResultsFragment.DEALT_DAMAGE, dealtDamage.values.toIntArray())
        val doa = arrayListOf<Boolean>()
        dealtDamage.keys.forEach { doa.add(deadOrAliveCells[it] ?: false) }
        bundle.putBooleanArray(BattleResultsFragment.DEAD_OR_ALIVE, doa.toBooleanArray())
        windowChanger.onNext(Router.Window(Router.WindowType.BATTLE_RESULTS, bundle))
    }

    override fun goToHeroesScreen() {
        val bundle = Bundle()
        windowChanger.onNext(Router.Window(Router.WindowType.HERO_SCREEN, bundle))
    }

    companion object {
        private const val TAG = "RouterImpl"
    }
}
