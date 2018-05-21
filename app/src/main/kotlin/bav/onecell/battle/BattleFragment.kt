package bav.onecell.battle

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.model.BattleFieldSnapshot
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import kotlinx.android.synthetic.main.fragment_battle.battleCanvasView
import kotlinx.android.synthetic.main.fragment_battle.buttonFinishBattle
import kotlinx.android.synthetic.main.fragment_battle.buttonFullStep
import kotlinx.android.synthetic.main.fragment_battle.buttonPartialStep
import kotlinx.android.synthetic.main.fragment_battle.seekBar
import javax.inject.Inject

class BattleFragment : Fragment(), Battle.View {

    @Inject
    lateinit var hexMath: HexMath
    @Inject
    lateinit var presenter: Battle.Presenter

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_battle, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        buttonFullStep.setOnClickListener { presenter.doFullStep() }
        buttonFinishBattle.setOnClickListener { presenter.finishBattle() }
        buttonPartialStep.setOnClickListener { presenter.doPartialStep() }

        seekBar.setOnSeekBarChangeListener(seekBarListener)

        buttonFullStep.isEnabled = true
        buttonPartialStep.isEnabled = true

        battleCanvasView.hexMath = hexMath
        battleCanvasView.presenter = presenter
        presenter.initialize(arguments?.getIntegerArrayList(EXTRA_CELL_INDEXES) ?: arrayListOf())
    }
    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(BattleModule(this))
                .inject(this)
    }
    //endregion

    //region Overridden methods
    override fun setBackgroundFieldRadius(radius: Int) {
        battleCanvasView.backgroundFieldRadius = radius
        battleCanvasView.invalidate()
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

    override fun setCorpses(cells: List<Cell>) {
        battleCanvasView.corpses = cells
    }

    override fun reportBattleEnd() {
        Toast.makeText(activity, "Battle is over", Toast.LENGTH_SHORT).show()
        buttonFullStep.isEnabled = false
        buttonPartialStep.isEnabled = false
        buttonFinishBattle.visibility = View.VISIBLE
    }

    override fun setSnapshots(snapshots: List<BattleFieldSnapshot>) {
        battleCanvasView.snapshots = snapshots
    }
    //endregion

    companion object {
        private const val TAG = "BattleFragment"
        const val EXTRA_CELL_INDEXES = "cell_indexes"

        fun newInstance(bundle: Bundle?): BattleFragment {
            val fragment = BattleFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
