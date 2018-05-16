package bav.onecell.main

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.cellslist.CellsListFragment
import bav.onecell.common.router.Router
import bav.onecell.common.router.Router.WindowType.*
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
            BATTLE_CELLS_SELECTION -> TODO()
            BATTLE -> TODO()
            CELL_EDITOR -> EditorFragment.newInstance(window.args)
            RULES_EDITOR -> TODO()
            CONDITIONS_EDITOR -> TODO()
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.holder, fragment).addToBackStack(window.toString())
        ft.commit()
    }
    //endregion
}
