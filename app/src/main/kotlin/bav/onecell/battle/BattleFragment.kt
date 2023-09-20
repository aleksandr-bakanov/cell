package bav.onecell.battle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.util.Log
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
import bav.onecell.databinding.FragmentBattleBinding
import bav.onecell.model.BattleInfo
import bav.onecell.model.hexes.HexMath
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BattleFragment : Fragment(), Battle.View {

    @Inject lateinit var hexMath: HexMath
    @Inject lateinit var presenter: Battle.Presenter
    @Inject lateinit var drawUtils: DrawUtils
    @Inject lateinit var resourceProvider: Common.ResourceProvider
    @Inject lateinit var gameState: Common.GameState
    @Inject lateinit var analytics: Common.Analytics
    @Inject lateinit var objectPool: Common.ObjectPool
    @Inject lateinit var battleGraphics: Battle.FramesFactory

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!

    private val disposables = CompositeDisposable()
    private var nextScene: Int = 0
    private var reward: String = ""
    private var battleDuration: Long = 0
    private var currentTimestamp: Long = 0
    private var animationTimer: Disposable? = null
    private var lastSeekBarPosition: Int = 0

    private var battleInfo: BattleInfo? = null

    private var sceneId: String = ""

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                if (setCurrentTimestamp(TIME_BETWEEN_FRAMES_MS * progress)) {
                    drawFrame(currentTimestamp)
                    lastSeekBarPosition = progress
                }
                else {
                    seekBar?.let { it.progress = lastSeekBarPosition }
                }
            }
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun setTimestampAndDrawFrame(timestamp: Long) {
        if (!setCurrentTimestamp(timestamp)) return
        drawFrame(currentTimestamp)
    }

    private fun setCurrentTimestamp(timestamp: Long): Boolean {
        var newTimestamp = timestamp

        if (newTimestamp > battleDuration) newTimestamp = battleDuration
        if (newTimestamp < 0) newTimestamp = 0

        currentTimestamp = newTimestamp
        return true
    }

    private fun clickOnStepButton(newTimestamp: Long) {
        if (setCurrentTimestamp(newTimestamp)) {
            drawFrame(currentTimestamp)
            setSeekBarProgress(currentTimestamp)
            lastSeekBarPosition = binding.seekBar.progress
        }
    }

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        binding.buttonNextStep.setOnClickListener {
            clickOnStepButton(currentTimestamp + TIMESTAMP_STEP)
        }
        binding.buttonPreviousStep.setOnClickListener {
            clickOnStepButton(currentTimestamp - TIMESTAMP_STEP)
        }

        binding.buttonPlay.setOnClickListener { startAnimation() }
        binding.buttonPause.setOnClickListener { pauseAnimation() }

        binding.seekBar.setOnSeekBarChangeListener(seekBarListener)

        binding.battleCanvasView.inject(hexMath, drawUtils)
        /// TODO: move to inject()
        binding.battleCanvasView.presenter = presenter
        binding.battleCanvasView.objectPool = objectPool
        binding.battleCanvasView.battleGraphics = battleGraphics

        binding.seekBar.max = 0
        disposables.addAll(
                presenter.battleResultsProvider()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { battleInfo ->
                            this.battleInfo = battleInfo
                            binding.battleCanvasView.battleInfo = battleInfo

                            if (battleInfo.snapshots.size > 0) {
                                binding.battleCanvasView.backgroundFieldRadius = battleInfo.snapshots[0].cells.asSequence().map { it.size() }.sum()
                            }

                            battleDuration = battleInfo.snapshots.sumBy { it.duration() }.toLong()
                            binding.seekBar.max = (0..battleDuration step TIME_BETWEEN_FRAMES_MS).count()

                            reportBattleEnd(battleInfo)

                            setTimestampAndDrawFrame(0)
                            binding.splashImage.visibility = View.GONE
                            binding.calculationTextView.visibility = View.GONE
                        })

        arguments?.let {
            val info = JSONObject(getString(it.getInt(EXTRA_PARAMS)))
            val battleParams = info.getString(BATTLE_PARAMS)
            val battleGroundResource = resourceProvider.getDrawableIdentifier(info.getString(BATTLE_GROUND_RESOURCE))
            sceneId = info.getString(Consts.SCENE_ID)

            drawUtils.setGroundShader(battleGroundResource)
            nextScene = resourceProvider.getIdIdentifier(info.getString(NEXT_SCENE))
            reward = info.optString(Consts.BATTLE_REWARD)
            gameState.setSceneAppeared(sceneId)
            presenter.initialize(battleParams)
        }
        binding.battleCanvasView.backgroundFieldRadius = 50
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
        pauseAnimation()
        disposables.dispose()
        presenter.stopBattleEvaluation()
        super.onDestroyView()
        _binding = null
    }
    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(BattleModule(this))
                .inject(this)
    }

    private fun setSeekBarProgress(timestamp: Long) {
        binding.seekBar.progress = (timestamp / TIME_BETWEEN_FRAMES_MS).toInt()
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
        reportBundle.putString(Consts.SCENE_ID, sceneId)

        binding.buttonFinishBattle.setOnClickListener { view ->
            view.findNavController().navigate(nextScene, reportBundle)
        }
        binding.buttonFinishBattle.visibility = View.VISIBLE
    }

    private fun startAnimation() {
        animationTimer = Observable.interval(0L, TIME_BETWEEN_FRAMES_MS, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (setCurrentTimestamp(currentTimestamp + TIME_BETWEEN_FRAMES_MS)) {
                        drawFrame(currentTimestamp)
                        setSeekBarProgress(currentTimestamp)
                        lastSeekBarPosition = binding.seekBar.progress
                        if (currentTimestamp >= battleDuration) {
                            pauseAnimation()
                        }
                    }
                }
        binding.buttonPause.visibility = View.VISIBLE
        binding.buttonPlay.visibility = View.INVISIBLE
    }

    private fun pauseAnimation() {
        animationTimer?.dispose()
        binding.buttonPause.visibility = View.INVISIBLE
        binding.buttonPlay.visibility = View.VISIBLE
    }

    private fun drawFrame(timestamp: Long) {
        binding.battleCanvasView.drawFrame(timestamp)
    }
    //endregion

    companion object {
        private const val TAG = "BattleFragment"
        private const val SCREEN_NAME = "Battle screen"
        const val EXTRA_PARAMS = "params"
        private const val TIMESTAMP_STEP: Long = 100
    }
}
