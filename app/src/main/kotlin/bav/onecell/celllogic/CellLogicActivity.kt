package bav.onecell.celllogic

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.main.MainModule
import bav.onecell.model.hexes.HexMath
import javax.inject.Inject

class CellLogicActivity : FragmentActivity(), CellLogic.View, CellLogic.PresenterProvider {
    companion object {
        private const val EXTRA_CELL_INDEX = "bav.onecell.extra_cell_index"

        fun newIntent(context: Context, cellIndex: Int): Intent {
            val extras = Bundle()
            extras.putInt(EXTRA_CELL_INDEX, cellIndex)
            val intent = Intent(context, CellLogicActivity::class.java)
            intent.putExtras(extras)
            return intent
        }
    }

    @Inject lateinit var presenter: CellLogic.Presenter

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish()
            return
        }

        setContentView(R.layout.activity_celllogic)
        inject()
        presenter.initialize(intent.getIntExtra(EXTRA_CELL_INDEX, -1))
    }
    //endregion

    //region Private methods
    private fun inject() {
        (application as OneCellApplication).appComponent
                .plus(MainModule())
                .inject(this)
    }
    //endregion

    //region Overridden methods
    override fun provideCellLogicPresenter(): CellLogic.Presenter = presenter
    //endregion
}