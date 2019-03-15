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
import bav.onecell.battle.BattleGraphics.Companion.TIME_BETWEEN_FRAMES_MS
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.Consts.Companion.BATTLE_GROUND_RESOURCE
import bav.onecell.common.Consts.Companion.BATTLE_PARAMS
import bav.onecell.common.Consts.Companion.NEXT_SCENE
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.BattleInfo
import bav.onecell.model.battle.FrameGraphics
import bav.onecell.model.hexes.HexMath
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
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
    private var frameGenerationJob: Job? = null
    private val frames: MutableMap<Long, FrameGraphics> = mutableMapOf()

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                setTimestampAndDrawFrame(TIME_BETWEEN_FRAMES_MS * progress)
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
        battleCanvasView.frames = frames

        seekBar.max = 0
        disposables.addAll(
                presenter.battleResultsProvider()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { battleInfo ->
                            if (battleInfo.snapshots.size > 0) {
                                battleCanvasView.backgroundFieldRadius = battleInfo.snapshots[0].cells.asSequence().map { it.size() }.sum()
                            }

                            battleDuration = battleInfo.snapshots.sumBy { it.duration() }.toLong()
                            seekBar.max = (0..battleDuration step TIME_BETWEEN_FRAMES_MS).count()

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
                                                        if (it.second != null) {
                                                            frames[it.first] = it.second!!
                                                            if (frames.size == 1) {
                                                                setTimestampAndDrawFrame(0)
                                                            }
                                                        }
                                                        else {
                                                            progressBar.progress = progressBar.max
                                                            reportBattleEnd(battleInfo)
                                                        }
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
        frameGenerationJob?.cancel()
        frameGenerationJob = null
        pauseAnimation()
        disposables.dispose()
        clearBattleFrames()
        super.onDestroyView()
    }
    //endregion

    //region Private methods
    private fun clearBattleFrames() {
        frames.values.forEach { it.clear() }
        frames.clear()
        battleCanvasView.frames = null
        System.gc()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(BattleModule(this))
                .inject(this)
    }

    private fun setSeekBarProgress(timestamp: Long) {
        seekBar.progress = (timestamp / TIME_BETWEEN_FRAMES_MS).toInt()
    }

    private val reportBundle = Bundle()
    private fun reportBattleEnd(battleInfo: BattleInfo) {
        val dealtDamage: Map<Int, Int> = battleInfo.damageDealtByCells
        val deadOrAliveCells: Map<Int, Boolean> = battleInfo.deadOrAliveCells
        reportBundle.clear()
        reportBundle.putIntArray(BattleResultsFragment.CELL_INDEXES, dealtDamage.keys.toIntArray())
        reportBundle.putIntArray(BattleResultsFragment.DEALT_DAMAGE, dealtDamage.values.toIntArray())
        val doa = arrayListOf<Boolean>()
        dealtDamage.keys.forEach { doa.add(deadOrAliveCells[it] ?: false) }
        reportBundle.putBooleanArray(BattleResultsFragment.DEAD_OR_ALIVE, doa.toBooleanArray())
        reportBundle.putBoolean(BattleResultsFragment.IS_BATTLE_WON, battleInfo.winnerGroupId == Consts.HERO_GROUP_ID)
        reportBundle.putString(Consts.BATTLE_REWARD, reward)

        battleInfo.clear()

        buttonFinishBattle.setOnClickListener { view ->
            view.findNavController().navigate(nextScene, reportBundle)
        }
        buttonFinishBattle.visibility = View.VISIBLE
    }

    private fun startAnimation() {
        animationTimer = Observable.interval(0L, TIME_BETWEEN_FRAMES_MS, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    currentTimestamp += TIME_BETWEEN_FRAMES_MS
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
        if (frames.containsKey(timestamp)) battleCanvasView.drawFrame(timestamp)
    }
    //endregion

    companion object {
        private const val TAG = "BattleFragment"
        private const val SCREEN_NAME = "Battle screen"
        const val EXTRA_PARAMS = "params"
        private const val TIMESTAMP_STEP: Long = 100

        fun newInstance(bundle: Bundle?): BattleFragment {
            val fragment = BattleFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
