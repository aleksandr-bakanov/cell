package bav.onecell.battle

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.HexMath
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_battle.battleCanvasView
import kotlinx.android.synthetic.main.fragment_battle.buttonFinishBattle
import kotlinx.android.synthetic.main.fragment_battle.buttonNextStep
import kotlinx.android.synthetic.main.fragment_battle.buttonPreviousStep
import kotlinx.android.synthetic.main.fragment_battle.seekBar
import javax.inject.Inject

class BattleFragment : Fragment(), Battle.View {

    @Inject lateinit var hexMath: HexMath
    @Inject lateinit var presenter: Battle.Presenter
    @Inject lateinit var drawUtils: DrawUtils

    private val disposables = CompositeDisposable()

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                updateBattleView(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_battle, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        buttonFinishBattle.setOnClickListener { presenter.finishBattle() }
        buttonNextStep.setOnClickListener { _ ->
            battleCanvasView.snapshots?.let {
                val next = battleCanvasView.currentSnapshotIndex + 1
                if (next < it.size) {
                    seekBar.progress = next
                    animateOneSnapshot(next)
                }
            }

        }
        buttonPreviousStep.setOnClickListener {
            val previous = battleCanvasView.currentSnapshotIndex - 1
            if (previous >= 0) {
                seekBar.progress = previous
                animateOneSnapshot(previous)
            }
        }

        seekBar.setOnSeekBarChangeListener(seekBarListener)

        battleCanvasView.hexMath = hexMath
        battleCanvasView.drawUtils = drawUtils
        battleCanvasView.presenter = presenter

        seekBar.max = 0
        disposables.addAll(
                presenter.battleResultsProvider()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            seekBar.max = it.snapshots.size - 1
                            battleCanvasView.snapshots = it.snapshots
                            battleCanvasView.invalidate()
                            reportBattleEnd()
                        })

        presenter.initialize(arguments?.getIntegerArrayList(EXTRA_CELL_INDEXES) ?: arrayListOf())
        battleCanvasView.backgroundFieldRadius = 5
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }
    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(BattleModule(this))
                .inject(this)
    }

    private fun reportBattleEnd() {
        activity?.runOnUiThread {
            Toast.makeText(activity, "Battle is over", Toast.LENGTH_SHORT).show()
            buttonFinishBattle.visibility = View.VISIBLE
        }
    }

    private fun animateOneSnapshot(snapshotIndex: Int) {
        battleCanvasView.snapshots?.let {
            val snapshot = it[snapshotIndex]

            // Draw initial state
            updateBattleView(snapshotIndex)

            // Apply cell actions


            // Apply moving animations for every living cell
            snapshot.cells.forEachIndexed { index, cell ->
                if (index >= 0 && index < snapshot.movingDirections.size)
                    animateCellMoving(cell, snapshot.movingDirections[index])
            }

            // Remove hexes destroyed at this round

        }
    }

    private fun animateCellMoving(cell: Cell, direction: Int) {
        // Clear cell moving fraction
        cell.animationData.movingFraction = 0f
        // Save move direction
        cell.animationData.moveDirection = direction
        // Start animation
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = CELL_MOVING_DURATION_MS
            addUpdateListener {
                cell.animationData.movingFraction = it.animatedValue as Float
                battleCanvasView.invalidate()
            }
            start()
        }
    }
    //endregion

    //region Overridden methods
    override fun updateBattleView(snapshotIndex: Int) {
        battleCanvasView.currentSnapshotIndex = snapshotIndex
        battleCanvasView.invalidate()
    }
    //endregion

    companion object {
        private const val TAG = "BattleFragment"
        const val EXTRA_CELL_INDEXES = "cell_indexes"

        const val CELL_MOVING_DURATION_MS: Long = 1000

        fun newInstance(bundle: Bundle?): BattleFragment {
            val fragment = BattleFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
