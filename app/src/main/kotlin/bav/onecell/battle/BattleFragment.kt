package bav.onecell.battle

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.animation.AnimatorSet
import android.widget.SeekBar
import androidx.navigation.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.battle.results.BattleResultsFragment
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.BattleInfo
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.HexMath
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import bav.onecell.model.cell.logic.Action
import kotlinx.android.synthetic.main.fragment_battle.battleCanvasView
import kotlinx.android.synthetic.main.fragment_battle.buttonFinishBattle
import kotlinx.android.synthetic.main.fragment_battle.buttonNextStep
import kotlinx.android.synthetic.main.fragment_battle.buttonPreviousStep
import kotlinx.android.synthetic.main.fragment_battle.seekBar

class BattleFragment : Fragment(), Battle.View {

    @Inject lateinit var hexMath: HexMath
    @Inject lateinit var presenter: Battle.Presenter
    @Inject lateinit var drawUtils: DrawUtils

    private val disposables = CompositeDisposable()

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                animateOneSnapshot(progress)
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

        buttonNextStep.setOnClickListener { _ ->
            battleCanvasView.snapshots?.let {
                val next = battleCanvasView.currentSnapshotIndex + 1
                if (next < it.size - 1) {
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
                        .subscribe { battleInfo ->
                            seekBar.max = battleInfo.snapshots.size - 2
                            battleCanvasView.snapshots = battleInfo.snapshots
                            battleCanvasView.invalidate()
                            animateOneSnapshot(0)
                            reportBattleEnd(battleInfo)
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

    private fun reportBattleEnd(battleInfo: BattleInfo) {
        buttonFinishBattle.setOnClickListener { view ->
            val dealtDamage: Map<Int, Int> = battleInfo.damageDealtByCells
            val deadOrAliveCells: Map<Int, Boolean> = battleInfo.deadOrAliveCells
            val bundle = Bundle()
            bundle.putIntArray(BattleResultsFragment.CELL_INDEXES, dealtDamage.keys.toIntArray())
            bundle.putIntArray(BattleResultsFragment.DEALT_DAMAGE, dealtDamage.values.toIntArray())
            val doa = arrayListOf<Boolean>()
            dealtDamage.keys.forEach { doa.add(deadOrAliveCells[it] ?: false) }
            bundle.putBooleanArray(BattleResultsFragment.DEAD_OR_ALIVE, doa.toBooleanArray())
            view.findNavController().navigate(R.id.action_battleFragment_to_battleResultsFragment, bundle)
            //presenter.finishBattle(battleInfo)
        }
        activity?.runOnUiThread {
            buttonFinishBattle.visibility = View.VISIBLE
        }
    }

    private fun animateOneSnapshot(snapshotIndex: Int) {
        battleCanvasView.snapshots?.let {
            val snapshot = it[snapshotIndex]

            // Draw initial state
            drawSnapshotInitialState(snapshotIndex)


            // Apply cell actions
            val cellLogicAnimators = mutableListOf<Animator>()
            snapshot.cells.forEachIndexed { index, cell ->
                if (index >= 0 && index < snapshot.cellsActions.size) {
                    snapshot.cellsActions[index]?.let { action ->
                        cellLogicAnimators.add(animateCellAction(cell, action))
                    }
                }
            }
            val cellLogicAnimatorSet = AnimatorSet()
            cellLogicAnimatorSet.playTogether(cellLogicAnimators)


            // Apply moving animations for every living cell
            val movingAnimators = mutableListOf<Animator>()
            snapshot.cells.forEachIndexed { index, cell ->
                if (index >= 0 && index < snapshot.movingDirections.size)
                    movingAnimators.add(animateCellMoving(cell, snapshot.movingDirections[index]))
            }
            val movingAnimatorSet = AnimatorSet()
            movingAnimatorSet.playTogether(movingAnimators)


            // Remove hexes destroyed at this round
            val hexRemovalAnimators = mutableListOf<Animator>()
            snapshot.cells.forEachIndexed { index, cell ->
                if (index >= 0 && index < snapshot.hexesToRemove.size)
                    hexRemovalAnimators.add(animateDeadHexesRemoval(cell, snapshot.hexesToRemove[index]))
            }
            val hexRemovalAnimatorSet = AnimatorSet()
            hexRemovalAnimatorSet.playTogether(hexRemovalAnimators)


            // Play snapshot
            val logicAndMovement = AnimatorSet()
            logicAndMovement.play(cellLogicAnimatorSet).before(movingAnimatorSet)

            val snapshotAnimators = AnimatorSet()
            snapshotAnimators.play(logicAndMovement).before(hexRemovalAnimatorSet)
            snapshotAnimators.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    snapshot.cells.forEach { c ->
                        with (c.animationData) {
                            movingFraction = 0f
                            fadeFraction = 0f
                            rotation = 0f
                        }
                    }
                    if (snapshotIndex + 1 < it.size) {
                        // Draw next state
                        battleCanvasView.fallBackToPreviousSnapshot = true
                        drawSnapshotInitialState(snapshotIndex + 1)
                    }
                    super.onAnimationEnd(animation)
                }
            })
            snapshotAnimators.start()
        }
    }

    private fun animateCellMoving(cell: Cell, direction: Int): Animator {
        // Clear cell moving fraction
        cell.animationData.movingFraction = 0f
        // Save move direction
        cell.animationData.moveDirection = direction
        // Start animation
        return ValueAnimator.ofFloat(0f, 1f).apply {
            duration = CELL_MOVING_DURATION_MS
            addUpdateListener {
                cell.animationData.movingFraction = it.animatedValue as Float
                battleCanvasView.invalidate()
            }
        }
    }

    private fun animateDeadHexesRemoval(cell: Cell, hexHashes: List<Int>): Animator {
        cell.animationData.fadeFraction = 0f
        return ValueAnimator.ofFloat(0f, 1f).apply {
            duration = if (hexHashes.isNotEmpty()) HEX_FADING_DURATION_MS else 0
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)
                    cell.animationData.hexHashesToRemove = hexHashes
                }
            })
            addUpdateListener {
                cell.animationData.fadeFraction = it.animatedValue as Float
                battleCanvasView.invalidate()
            }
        }
    }

    private fun animateCellAction(cell: Cell, action: Action): Animator {
        return when (action.act) {
            Action.Act.CHANGE_DIRECTION -> animateCellRotation(cell, action.value)
        }
    }

    private fun animateCellRotation(cell: Cell, direction: Int): Animator {
        cell.animationData.rotation = 0f
        val angle = cell.getRotationAngle(direction)
        return ValueAnimator.ofFloat(0f, cell.getRotationAngle(direction)).apply {
            duration = if (angle != 0f) CELL_ROTATION_DURATION_MS else 0
            addUpdateListener {
                cell.animationData.rotation = it.animatedValue as Float
                battleCanvasView.invalidate()
            }
        }
    }
    //endregion

    //region Overridden methods
    override fun drawSnapshotInitialState(snapshotIndex: Int) {
        battleCanvasView.currentSnapshotIndex = snapshotIndex
        battleCanvasView.invalidate()
    }
    //endregion

    companion object {
        private const val TAG = "BattleFragment"
        const val EXTRA_CELL_INDEXES = "cell_indexes"

        const val CELL_MOVING_DURATION_MS: Long = 500
        const val HEX_FADING_DURATION_MS: Long = 500
        const val CELL_ROTATION_DURATION_MS: Long = 500

        fun newInstance(bundle: Bundle?): BattleFragment {
            val fragment = BattleFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
