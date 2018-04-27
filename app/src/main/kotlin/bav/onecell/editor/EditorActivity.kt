package bav.onecell.editor

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

class EditorActivity : FragmentActivity(), Editor.View, EditorFragment.OnEditorFragmentInteractionListener {
    companion object {
        private const val EXTRA_CELL_INDEX = "bav.onecell.extra_cell_index"

        fun newIntent(context: Context, cellIndex: Int): Intent {
            val extras = Bundle()
            extras.putInt(EXTRA_CELL_INDEX, cellIndex)
            val intent = Intent(context, EditorActivity::class.java)
            intent.putExtras(extras)
            return intent
        }
    }

    @Inject lateinit var presenter: Editor.Presenter
    @Inject lateinit var hexMath: HexMath

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish()
            return
        }

        setContentView(R.layout.activity_editor)
        inject()
        presenter.initialize(intent.getIntExtra(EXTRA_CELL_INDEX, -1))
    }
    //endregion

    //region Private methods
    private fun inject() {
        (application as OneCellApplication).appComponent
                .plus(MainModule(null))
                .inject(this)
    }
    //endregion

    //region Overridden methods
    override fun provideEditorPresenter(): Editor.Presenter = presenter
    override fun provideHexMath(): HexMath = hexMath
    //endregion
}