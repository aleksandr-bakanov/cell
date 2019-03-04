package bav.onecell.battle

import android.graphics.Path
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
import bav.onecell.common.Consts.Companion.BATTLE_GROUND_RESOURCE
import bav.onecell.common.Consts.Companion.BATTLE_PARAMS
import bav.onecell.common.Consts.Companion.NEXT_SCENE
import bav.onecell.common.extensions.visible
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.BattleInfo
import bav.onecell.model.battle.FrameGraphics
import bav.onecell.model.hexes.HexMath
import bav.onecell.model.hexes.Point
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
import kotlinx.android.synthetic.main.fragment_battle.progressBar
import kotlinx.android.synthetic.main.fragment_battle.seekBar
import kotlinx.android.synthetic.main.fragment_battle.splashImage
import kotlinx.coroutines.Job
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BattleFragment : Fragment(), Battle.View {

    @Inject lateinit var hexMath: HexMath
    @Inject lateinit var presenter: Battle.Presenter
    @Inject lateinit var drawUtils: DrawUtils
    @Inject lateinit var resourceProvider: Common.ResourceProvider
    @Inject lateinit var gameState: Common.GameState
    @Inject lateinit var analytics: Common.Analytics
    @Inject lateinit var framesFactory: Battle.FramesFactory

    private val disposables = CompositeDisposable()
    private var nextScene: Int = 0
    private var reward: String = ""
    private var battleDuration: Long = 0
    private var currentTimestamp: Long = 0
    private var animationTimer: Disposable? = null
    private var isBattleWon = false
    private var isFog = false
    private var frames: Map<Long, FrameGraphics>? = null
    private var frameGenerationJob: Job? = null

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
                            if (battleInfo.snapshots.size > 0) {
                                battleCanvasView.backgroundFieldRadius = battleInfo.snapshots[0].cells.asSequence().map { it.size() }.sum()
                            }

                            disposables.add(framesFactory.progressProvider()
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe {
                                                        progressBar.progress = it
                                                    })

                            disposables.add(framesFactory.framesProvider()
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe {
                                                        frames = it
                                                        battleCanvasView.frames = it
                                                        seekBar.max = frames?.size ?: 0
                                                        battleDuration = frames?.keys?.max() ?: 0
                                                        setTimestampAndDrawFrame(0)
                                                        progressBar.visibility = View.INVISIBLE
                                                        splashImage.visibility = View.INVISIBLE

                                                        reportBattleEnd(battleInfo)
                                                    })
                            frameGenerationJob = framesFactory.generateFrames(battleInfo)
                        })

        arguments?.let {
            val info = JSONObject(getString(it.getInt(EXTRA_PARAMS)))
            val battleParams = info.getString(BATTLE_PARAMS)
            val battleGroundResource = resourceProvider.getDrawableIdentifier(info.getString(BATTLE_GROUND_RESOURCE))
            drawUtils.setGroundShader(battleGroundResource)
            nextScene = resourceProvider.getIdIdentifier(info.getString(NEXT_SCENE))
            reward = info.optString(Consts.BATTLE_REWARD)
            presenter.initialize(battleParams)
        }
        battleCanvasView.backgroundFieldRadius = 50
    }

    override fun onResume() {
        super.onResume()
        analytics.setCurrentScreen(requireActivity(), SCREEN_NAME, this::class.qualifiedName)
    }

    override fun onPause() {
        gameState.setLastNavDestinationId(findNavController().currentDestination?.id ?: 0)
        super.onPause()
    }

    override fun onDestroyView() {
        disposables.dispose()
        pauseAnimation()
        frameGenerationJob?.cancel()
        frameGenerationJob = null
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

    private fun drawFrame(timestamp: Long) {
        battleCanvasView.drawFrame(timestamp)
    }
    //endregion

    companion object {
        private const val TAG = "BattleFragment"
        private const val SCREEN_NAME = "Battle screen"
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
