package bav.onecell.battle

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.battle.results.BattleResultsFragment
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.Consts.Companion.BATTLE_PARAMS
import bav.onecell.common.Consts.Companion.NEXT_SCENE
import bav.onecell.common.extensions.visible
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.BattleInfo
import bav.onecell.model.hexes.HexMath
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import bav.onecell.model.cell.logic.Action
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_battle.battleCanvasView
import kotlinx.android.synthetic.main.fragment_battle.buttonFinishBattle
import kotlinx.android.synthetic.main.fragment_battle.buttonNextStep
import kotlinx.android.synthetic.main.fragment_battle.buttonPause
import kotlinx.android.synthetic.main.fragment_battle.buttonPlay
import kotlinx.android.synthetic.main.fragment_battle.buttonPreviousStep
import kotlinx.android.synthetic.main.fragment_battle.seekBar
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BattleFragment : Fragment(), Battle.View {

    @Inject lateinit var hexMath: HexMath
    @Inject lateinit var presenter: Battle.Presenter
    @Inject lateinit var drawUtils: DrawUtils
    @Inject lateinit var resourceProvider: Common.ResourceProvider
    @Inject lateinit var gameState: Common.GameState

    private val disposables = CompositeDisposable()
    private var nextScene: Int = 0
    private var reward: String = ""
    private var battleDuration: Long = 0
    private var currentTimestamp: Long = 0
    private var animationTimer: Disposable? = null
    private var isBattleWon = false
    private var isFog = false

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                setTimestampAndDrawFrame(TIMESTAMP_ANIMATION_STEP * progress)
            }
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun setTimestampAndDrawFrame(timestamp: Long) {
        currentTimestamp = timestamp
        if (currentTimestamp > battleDuration) {
            currentTimestamp = battleDuration
        }
        if (currentTimestamp < 0) currentTimestamp = 0

        if (isFog && isBattleWon && currentTimestamp == battleDuration) {
            battleCanvasView.isFog = false
        }
        if (isFog && isBattleWon && currentTimestamp != battleDuration) {
            battleCanvasView.isFog = true
        }
        if (isFog && !isBattleWon) {
            battleCanvasView.isFog = true
        }
        drawFrame(currentTimestamp)
    }

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_battle, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        buttonNextStep.setOnClickListener {
            setTimestampAndDrawFrame(currentTimestamp + TIMESTAMP_STEP)
            setSeekBarProgress(currentTimestamp)
        }
        buttonPreviousStep.setOnClickListener {
            setTimestampAndDrawFrame(currentTimestamp - TIMESTAMP_STEP)
            setSeekBarProgress(currentTimestamp)
        }

        buttonPlay.setOnClickListener { startAnimation() }
        buttonPause.setOnClickListener { pauseAnimation() }

        seekBar.setOnSeekBarChangeListener(seekBarListener)

        battleCanvasView.inject(hexMath, drawUtils)
        battleCanvasView.presenter = presenter

        seekBar.max = 0
        disposables.addAll(
                presenter.battleResultsProvider()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { battleInfo ->
                            battleDuration = battleInfo.snapshots.sumBy { it.duration() }.toLong()
                            seekBar.max = (battleDuration / TIMESTAMP_ANIMATION_STEP).toInt() + 1
                            battleCanvasView.snapshots = battleInfo.snapshots
                            battleCanvasView.isFog = battleInfo.isFog
                            isFog = battleInfo.isFog
                            isBattleWon = battleInfo.winnerGroupId == Consts.HERO_GROUP_ID
                            if (battleInfo.snapshots.size > 0) {
                                battleCanvasView.backgroundFieldRadius = battleInfo.snapshots[0].cells.asSequence().map { it.size() }.sum()
                            }
                            battleCanvasView.invalidate()
                            drawFrame(currentTimestamp)
                            reportBattleEnd(battleInfo)
                        })

        arguments?.let {
            val info = JSONObject(getString(it.getInt(EXTRA_PARAMS)))
            val battleParams = info.getString(BATTLE_PARAMS)
            nextScene = resourceProvider.getIdIdentifier(info.getString(NEXT_SCENE))
            reward = info.optString(Consts.BATTLE_REWARD)
            presenter.initialize(battleParams)
        }
        battleCanvasView.backgroundFieldRadius = 50
    }

    override fun onPause() {
        gameState.setLastNavDestinationId(findNavController().currentDestination?.id ?: 0)
        super.onPause()
    }

    override fun onDestroyView() {
        disposables.dispose()
        pauseAnimation()
        super.onDestroyView()
    }
    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(BattleModule(this))
                .inject(this)
    }

    private fun setSeekBarProgress(timestamp: Long) {
        seekBar.progress = (timestamp / TIMESTAMP_ANIMATION_STEP).toInt()
    }

    private fun reportBattleEnd(battleInfo: BattleInfo) {
        buttonFinishBattle.setOnClickListener { view ->
            // TODO: clear animations before leaving the screen
            val dealtDamage: Map<Int, Int> = battleInfo.damageDealtByCells
            val deadOrAliveCells: Map<Int, Boolean> = battleInfo.deadOrAliveCells
            val bundle = Bundle()
            bundle.putIntArray(BattleResultsFragment.CELL_INDEXES, dealtDamage.keys.toIntArray())
            bundle.putIntArray(BattleResultsFragment.DEALT_DAMAGE, dealtDamage.values.toIntArray())
            val doa = arrayListOf<Boolean>()
            dealtDamage.keys.forEach { doa.add(deadOrAliveCells[it] ?: false) }
            bundle.putBooleanArray(BattleResultsFragment.DEAD_OR_ALIVE, doa.toBooleanArray())
            bundle.putBoolean(BattleResultsFragment.IS_BATTLE_WON, battleInfo.winnerGroupId == Consts.HERO_GROUP_ID)
            bundle.putString(Consts.BATTLE_REWARD, reward)
            view.findNavController().navigate(nextScene, bundle)
        }
        buttonFinishBattle.visibility = View.VISIBLE
    }

    private fun startAnimation() {
        animationTimer = Observable.interval(0L, TIMESTAMP_ANIMATION_STEP, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    currentTimestamp += TIMESTAMP_ANIMATION_STEP
                    setTimestampAndDrawFrame(currentTimestamp)
                    setSeekBarProgress(currentTimestamp)
                    if (currentTimestamp >= battleDuration) {
                        pauseAnimation()
                    }
                }
        buttonPause.visibility = View.VISIBLE
        buttonPlay.visibility = View.INVISIBLE
    }

    private fun pauseAnimation() {
        animationTimer?.let { if (!it.isDisposed) it.dispose() }
        buttonPause.visibility = View.INVISIBLE
        buttonPlay.visibility = View.VISIBLE
    }

    data class FrameState(val snapshotIndex: Int, /*val actionFraction: Float,*/ val movingFraction: Float,
                          val deathRayFraction: Float, val hexRemovalFraction: Float)

    private fun getFrameState(timestamp: Long): FrameState {
        var acc = 0
        var snapshotIndex = -1
        battleCanvasView.snapshots?.let {
            for (i in 0 until it.size) {
                snapshotIndex++
                val snapshot = it[i]
                if (acc + snapshot.duration() > timestamp) break
                acc += snapshot.duration()
            }
        }

        val snapshot = battleCanvasView.snapshots?.get(snapshotIndex)!!

        /*val actionTime = timestamp - acc
        val actionFraction = animationTimeFraction(actionTime, snapshot.actionsDuration())
        acc += snapshot.actionsDuration()*/

        val movingTime = timestamp - acc
        val movingFraction = animationTimeFraction(movingTime, snapshot.movementDuration())
        acc += snapshot.movementDuration()

        val deathRayTime = timestamp - acc
        val deathRayFraction = animationTimeFraction(deathRayTime, snapshot.deathRaysDuration())
        acc += snapshot.deathRaysDuration()

        val hexRemovalTime = timestamp - acc
        val hexRemovalFraction = animationTimeFraction(hexRemovalTime, snapshot.hexRemovalDuration())

        return FrameState(snapshotIndex, /*actionFraction,*/ movingFraction, deathRayFraction, hexRemovalFraction)
    }

    private fun animationTimeFraction(time: Long, animationDuration: Int): Float {
        return if (animationDuration == 0) 0f
               else if (time < 0) 0f
               else if (time > animationDuration) 1f
               else time.toFloat() / animationDuration.toFloat()
    }

    private fun drawFrame(timestamp: Long) {
        val frameState = getFrameState(timestamp)

        battleCanvasView.currentSnapshotIndex = frameState.snapshotIndex
        battleCanvasView.snapshots?.get(frameState.snapshotIndex)?.let { snapshot ->
            // Actions
            snapshot.cells.forEachIndexed { index, cell ->
                // Reset actions data
                cell.animationData.rotation = 0f
                if (index >= 0 && index < snapshot.cellsActions.size) {
                    snapshot.cellsActions[index]?.let { action ->
                        when (action.act) {
                            Action.Act.CHANGE_DIRECTION -> {
                                val angle = cell.getRotationAngle(action.value)
                                cell.animationData.rotation = if (angle == 0f) 0f else {
                                    cell.getRotationAngle(action.value) * /*frameState.actionFraction*/ frameState.movingFraction
                                }
                            }
                        }
                    }
                }
            }

            // Moving
            snapshot.cells.forEachIndexed { index, cell ->
                if (index >= 0 && index < snapshot.movingDirections.size) {
                    // Save move direction
                    cell.animationData.moveDirection = snapshot.movingDirections[index]
                    // Clear cell moving fraction
                    cell.animationData.movingFraction = frameState.movingFraction
                }
            }
            snapshot.bullets.forEach { bullet ->
                bullet.movingFraction = frameState.movingFraction
            }

            // Death rays
            battleCanvasView.deathRayFraction = if (snapshot.deathRays.isNotEmpty()) frameState.deathRayFraction else 0f

            // Hex removal
            snapshot.cells.forEachIndexed { index, cell ->
                if (index >= 0 && index < snapshot.hexesToRemove.size) {
                    cell.animationData.hexHashesToRemove = snapshot.hexesToRemove[index]
                    cell.animationData.fadeFraction = frameState.hexRemovalFraction
                }
            }
        }

        battleCanvasView.invalidate()
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
        const val EXTRA_PARAMS = "params"
        private const val TIMESTAMP_STEP: Long = 100
        private const val TIMESTAMP_ANIMATION_STEP: Long = 20 // 16 for ~60 fps; 25 for 40 fps; 20 for 50 fps

        fun newInstance(bundle: Bundle?): BattleFragment {
            val fragment = BattleFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
