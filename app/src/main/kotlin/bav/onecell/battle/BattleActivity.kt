package bav.onecell.battle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.model.Cell
import bav.onecell.model.hexes.Hex
import kotlinx.android.synthetic.main.activity_battle.battleCanvasView
import javax.inject.Inject

class BattleActivity : Activity(), Battle.View {
    companion object {
        private const val EXTRA_CELL_INDEXES = "bav.onecell.extra_cell_indexes"

        fun newIntent(context: Context, cellIndexes: List<Int>): Intent {
            val extras = Bundle()
            extras.putIntegerArrayList(EXTRA_CELL_INDEXES, ArrayList(cellIndexes))
            val intent = Intent(context, BattleActivity::class.java)
            intent.putExtras(extras)
            return intent
        }
    }

    @Inject
    lateinit var presenter: Battle.Presenter

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)
        inject()
        battleCanvasView.presenter = presenter
        presenter.initialize(intent.getIntegerArrayListExtra(EXTRA_CELL_INDEXES))
    }
    //endregion

    //region View listeners
    fun onNextStepButtonClicked(view: View) {
        presenter.doNextStep()
    }

    //region Private methods
    private fun inject() {
        (application as OneCellApplication).appComponent
                .plus(BattleModule(this))
                .inject(this)
    }
    //endregion

    //region Overridden methods
    override fun setBackgroundFieldRadius(radius: Int) {
        battleCanvasView.backgroundFieldRadius = radius
    }

    override fun updateBattleView() {
        battleCanvasView.invalidate()
    }

    override fun setRing(ring: List<Hex>) {
        battleCanvasView.ring = ring
    }

    override fun setCells(cells: List<Cell>) {
        battleCanvasView.cells = cells
    }
    //endregion
}