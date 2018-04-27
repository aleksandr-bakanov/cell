package bav.onecell.main

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import bav.onecell.OneCellApplication
import bav.onecell.R
import javax.inject.Inject

class MainActivity : FragmentActivity(), Main.View, CellListFragment.OnCellListFragmentInteractionListener {

    @Inject
    lateinit var presenter: Main.Presenter

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inject()
        presenter.initialize()
    }

    override fun onPause() {
        presenter.onPause()
        super.onPause()
    }
    //endregion

    //region Private methods
    private fun inject() {
        (application as OneCellApplication).appComponent
                .plus(MainModule(this))
                .inject(this)
    }
    //endregion

    //region Overridden methods
    override fun providePresenter(): Main.Presenter = presenter
    //endregion
}
