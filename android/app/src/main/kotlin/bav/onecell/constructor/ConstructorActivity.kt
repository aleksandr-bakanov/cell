package bav.onecell.constructor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import bav.onecell.OneCellApplication
import javax.inject.Inject

class ConstructorActivity : Activity(), Constructor.View {
    companion object {
        private const val EXTRA_CELL_INDEX = "bav.onecell.extra_cell_index"

        fun newIntent(context: Context, cellIndex: Int): Intent {
            val extras = Bundle()
            extras.putInt(EXTRA_CELL_INDEX, cellIndex)
            val intent = Intent(context, ConstructorActivity::class.java)
            intent.putExtras(extras)
            return intent
        }
    }

    @Inject
    lateinit var presenter: Constructor.Presenter

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject()
        presenter.initialize(intent.getIntExtra(EXTRA_CELL_INDEX, -1))
    }
    //endregion

    //region Private methods
    private fun inject() {
        (application as OneCellApplication).appComponent
                .plus(ConstructorModule(this))
                .inject(this)
    }
    //endregion
}