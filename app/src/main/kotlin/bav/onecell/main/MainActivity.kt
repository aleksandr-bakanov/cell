package bav.onecell.main

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.celllogic.CellLogic
import bav.onecell.celllogic.RuleListFragment
import bav.onecell.editor.Editor
import bav.onecell.editor.EditorFragment
import bav.onecell.model.hexes.HexMath
import javax.inject.Inject

class MainActivity : FragmentActivity(), Main.View, CellListFragment.OnCellListFragmentInteractionListener,
    EditorFragment.OnEditorFragmentInteractionListener, CellLogic.PresenterProvider {

    @Inject lateinit var mainPresenter: Main.Presenter

    @Inject lateinit var editorPresenter: Editor.Presenter
    @Inject lateinit var hexMath: HexMath

    @Inject lateinit var cellLogicPresenter: CellLogic.Presenter

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inject()
        mainPresenter.initialize()
    }

    override fun onPause() {
        mainPresenter.onPause()
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
    override fun provideMainPresenter(): Main.Presenter = mainPresenter
    override fun isLandscape(): Boolean = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    override fun provideEditorPresenter(): Editor.Presenter = editorPresenter
    override fun provideHexMath(): HexMath = hexMath

    override fun provideCellLogicPresenter(): CellLogic.Presenter = cellLogicPresenter

    override fun openCellEditorView(cellIndex: Int) {
        editorPresenter.initialize(cellIndex)
        // TODO: add check that cellIndex is currently shown, therefore no need to create new fragment
        val editorFragment = EditorFragment()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.editor, editorFragment)
        ft.commit()
    }

    override fun openCellLogicEditorView(cellIndex: Int) {
        cellLogicPresenter.initialize(cellIndex)
        // TODO: add check that cellIndex is currently shown, therefore no need to create new fragment
        val rulesFragment = RuleListFragment.newInstance()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.editor, rulesFragment)
        ft.commit()
    }
    //endregion
}
