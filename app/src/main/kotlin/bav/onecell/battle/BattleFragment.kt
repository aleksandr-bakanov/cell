package bav.onecell.battle

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import kotlinx.android.synthetic.main.fragment_battle.battleCanvasView
import kotlinx.android.synthetic.main.fragment_battle.buttonFinishBattle
import kotlinx.android.synthetic.main.fragment_battle.buttonFullStep
import kotlinx.android.synthetic.main.fragment_battle.buttonPartialStep
import javax.inject.Inject

class BattleFragment : Fragment(), Battle.View {

    @Inject
    lateinit var hexMath: HexMath
    @Inject
    lateinit var presenter: Battle.Presenter

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "$this onCreateView")
        return inflater.inflate(R.layout.fragment_battle, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "$this onActivityCreated")
        inject()

        buttonFullStep.setOnClickListener { presenter.doFullStep() }
        buttonFinishBattle.setOnClickListener { presenter.finishBattle() }
        buttonPartialStep.setOnClickListener { presenter.doPartialStep() }

        buttonFullStep.isEnabled = true
        buttonPartialStep.isEnabled = true

        battleCanvasView.hexMath = hexMath
        battleCanvasView.presenter = presenter
        presenter.initialize(arguments?.getIntegerArrayList(EXTRA_CELL_INDEXES) ?: arrayListOf())
    }

    override fun onDestroyView() {
        Log.d(TAG, "$this onDestroyView")
        super.onDestroyView()
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, "$this onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "$this onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "$this onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "$this onResume")
    }

    override fun onPause() {
        Log.d(TAG, "$this onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "$this onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "$this onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG, "$this onDetach")
        super.onDetach()
    }
}
