package bav.onecell.main

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.cellslist.CellsListFragment
import bav.onecell.common.router.Router
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
        changeWindow(Router.Window.MAIN)

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
        val fragment = when (window) {
            Router.Window.MAIN -> MainFragment.newInstance()
            Router.Window.CELLS_LIST -> CellsListFragment.newInstance()
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.holder, fragment)
        ft.commit()
    }
    //endregion
}
