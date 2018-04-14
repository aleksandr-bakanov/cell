package bav.onecell.constructor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.model.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import kotlinx.android.synthetic.main.activity_constructor.constructorCanvasView
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

    @Inject lateinit var presenter: Constructor.Presenter
    @Inject lateinit var hexMath: HexMath

    // TODO: same variable exists in ConstructorCanvasView, it is unnecessary duplicate
    private var selectedCellType: Hex.Type = Hex.Type.LIFE

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constructor)
        inject()
        constructorCanvasView.hexMath = hexMath
        constructorCanvasView.presenter = presenter
        presenter.initialize(intent.getIntExtra(EXTRA_CELL_INDEX, -1))
    }
    //endregion

    //region View listeners
    fun onCellTypeRadioButtonClicked(view: View) {
        selectedCellType = when (view.id) {
            R.id.radioButtonLifeCell -> Hex.Type.LIFE
            R.id.radioButtonEnergyCell -> Hex.Type.ENERGY
            R.id.radioButtonAttackCell -> Hex.Type.ATTACK
            else -> Hex.Type.REMOVE
        }
        constructorCanvasView.selectedCellType = selectedCellType
    }

    fun onCellRotateButtonClicked(view: View) {
        when (view.id) {
            R.id.buttonRotateCellLeft -> presenter.rotateCellLeft()
            R.id.buttonRotateCellRight -> presenter.rotateCellRight()
        }
        constructorCanvasView.invalidate()
    }
    //endregion

    //region Private methods
    private fun inject() {
        (application as OneCellApplication).appComponent
                .plus(ConstructorModule(this))
                .inject(this)
    }
    //endregion

    //region Overridden methods
    override fun setBackgroundFieldRadius(radius: Int) {
        constructorCanvasView.backgroundFieldRadius = radius
        constructorCanvasView.invalidate()
    }

    override fun setCell(cell: Cell?) {
        constructorCanvasView.cell = cell
    }
    //endregion
}