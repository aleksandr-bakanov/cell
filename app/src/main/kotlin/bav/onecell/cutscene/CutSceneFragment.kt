package bav.onecell.cutscene

import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.Consts.Companion.GAME_STATE_CHANGES
import bav.onecell.common.extensions.visible
import bav.onecell.databinding.FragmentCutSceneBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CutSceneFragment : Fragment(), CutScene.View {

    @Inject lateinit var presenter: CutScene.Presenter
    @Inject lateinit var resourceProvider: Common.ResourceProvider
    @Inject lateinit var gameState: Common.GameState
    @Inject lateinit var analytics: Common.Analytics
    private val disposables = CompositeDisposable()

    private var _binding: FragmentCutSceneBinding? = null
    private val binding get() = _binding!!

    private var defaultBackground: Int = 0
    private var defaultLeftCharacter: Int = 0
    private var defaultRightCharacter: Int = 0
    private val frames: MutableMap<Int, FrameData> = mutableMapOf()
    private var nextScene: Int = 0
    private var yesNextScene: Int = 0
    private var noNextScene: Int = 0
    private var currentFrameIndex: Int = 0
    private var decisionMade: String = ""
    private var isDecisionFrame = false
    private var isFinalFrame = false
    private var cutSceneId: String = ""

    private var textAnimationDisposable: Disposable? = null
    private var currentFrameTextIndex: Int = 0
    private var currentFrameText: String? = null

    private var animationDisposable: Disposable? = null
    private var animationFrames: List<Int>? = null
    private var currentAnimationFrame: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentCutSceneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        binding.background.setOnClickListener { showNextFrame() }
        binding.buttonPreviousFrame.setOnClickListener { showPreviousFrame() }
        binding.buttonYes.setOnClickListener { makeDecision(true) }
        binding.buttonNo.setOnClickListener { makeDecision(false) }
        binding.textView.movementMethod = ScrollingMovementMethod()

        parseArguments(arguments)
    }

    override fun onStart() {
        super.onStart()
        if (!gameState.getAndDropIgnoreCutSceneShownStatus() && gameState.isCutSceneAlreadyShown(cutSceneId)) {
            findNavController().navigate(takeNextScene())
        }
    }

    override fun onResume() {
        super.onResume()
        analytics.setCurrentScreen(requireActivity(), "$SCREEN_NAME $cutSceneId", this::class.qualifiedName)
        currentFrameIndex = gameState.getCurrentFrame()
        showFrame(currentFrameIndex)
    }

    override fun onPause() {
        gameState.setCurrentFrame(currentFrameIndex)
        gameState.setLastNavDestinationId(findNavController().currentDestination?.id ?: 0)
        stopTextTimer()
        stopAnimationTimer()
        super.onPause()
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
        _binding = null
    }

    private fun stopTextTimer() {
        textAnimationDisposable?.let { if (!it.isDisposed) it.dispose() }
    }

    private fun stopAnimationTimer() {
        animationDisposable?.let { if (!it.isDisposed) it.dispose() }
        animationDisposable = null
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(CutSceneModule())
                .inject(this)
    }

    private fun getBackground(name: String): Int = if (name.isEmpty()) defaultBackground else resourceProvider.getDrawableIdentifier(name)
    private fun getLeftCharacter(name: String): Int = if (name.isEmpty()) defaultLeftCharacter else resourceProvider.getDrawableIdentifier(name)
    private fun getRightCharacter(name: String): Int = if (name.isEmpty()) defaultRightCharacter else resourceProvider.getDrawableIdentifier(name)

    private fun parseArguments(arguments: Bundle?) {
        arguments?.let {
            try {
                val info = JSONObject(getString(it.getInt(CUT_SCENE_INFO)))
                defaultBackground = resourceProvider.getDrawableIdentifier(info.optString(BACKGROUND))
                defaultLeftCharacter = resourceProvider.getDrawableIdentifier(info.optString(LEFT))
                defaultRightCharacter = resourceProvider.getDrawableIdentifier(info.optString(RIGHT))
                cutSceneId = info.optString(CUT_SCENE_ID)
                nextScene = resourceProvider.getIdIdentifier(info.optString(Consts.NEXT_SCENE))
                yesNextScene = resourceProvider.getIdIdentifier(info.optString(Consts.YES_NEXT_SCENE))
                noNextScene = resourceProvider.getIdIdentifier(info.optString(Consts.NO_NEXT_SCENE))
                val framesMap = info.getJSONObject(FRAMES)
                for (i in framesMap.keys()) {
                    val data = framesMap.getJSONObject(i)
                    val frameData = FrameData(text = data.optString(TEXT), background = data.optString(BACKGROUND),
                                          left = data.optString(LEFT), right = data.optString(RIGHT),
                                          nextFrame = data.optInt(NEXT_FRAME, DEFAULT_NEXT_FRAME),
                                          decisionField = data.optString(DECISION_FIELD),
                                          yesNextFrame = data.optInt(YES_NEXT_FRAME, DEFAULT_NEXT_FRAME),
                                          noNextFrame = data.optInt(NO_NEXT_FRAME, DEFAULT_NEXT_FRAME),
                                          showPrevFrameButton = data.optBoolean(SHOW_PREV_FRAME_BUTTON, false),
                                          isFinalFrame = data.optBoolean(FINAL_FRAME, false),
                                          textColor = resourceProvider.getColor(data.optString(COLOR)),
                                          animationStepMs = data.optLong(ANIMATION_STEP, ANIMATION_FRAME_STEP))
                    data.optJSONArray(ANIMATION)?.let { animation ->
                        frameData.animation = mutableListOf()
                        for (k in 0 until animation.length()) {
                            frameData.animation?.add(resourceProvider.getDrawableIdentifier(animation.getString(k)))
                        }
                    }
                    frames[i.toInt()] = frameData
                }
                info.optJSONObject(GAME_STATE_CHANGES)?.let { gameStateChanges ->
                    // Changes should contain booleans
                    for (decision in gameStateChanges.keys()) {
                        gameState.setDecision(decision, gameStateChanges.getBoolean(decision))
                    }
                }
                gameState.setSceneAppeared(cutSceneId)
            } catch (e: JSONException) {
                Log.e(TAG, "wrong json: $e")
            }
        }
    }

    private fun showFrame(index: Int) {
        frames[index]?.let {
            stopAnimationTimer()
            it.animation?.let { animation ->
                animationFrames = animation
                currentAnimationFrame = 0
                animationDisposable = Observable.interval(0L, it.animationStepMs, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (currentAnimationFrame == animationFrames?.size) {
                                currentAnimationFrame = 0
                            }
                            animationFrames?.let { anims ->
                                binding.background.setImageDrawable(ContextCompat.getDrawable(requireContext(), anims[currentAnimationFrame]))
                            }
                            currentAnimationFrame++
                        }
            } ?: binding.background.setImageDrawable(ContextCompat.getDrawable(requireContext(), getBackground(it.background)))

            binding.leftCharacter.setImageDrawable(ContextCompat.getDrawable(requireContext(), getLeftCharacter(it.left)))
            binding.rightCharacter.setImageDrawable(ContextCompat.getDrawable(requireContext(), getRightCharacter(it.right)))

            // TODO: don't give a choice if decision has been taken already
            binding.buttonYes.visible = it.decisionField.isNotEmpty()
            binding.buttonNo.visible = it.decisionField.isNotEmpty()
            isDecisionFrame = it.decisionField.isNotEmpty()
            isFinalFrame = it.isFinalFrame

            binding.buttonPreviousFrame.visibility = if (it.showPrevFrameButton) View.VISIBLE else View.GONE

            binding.textView.text = ""
            binding.textView.setTextColor(it.textColor)
            currentFrameText = resourceProvider.getString(it.text)
            currentFrameTextIndex = 0
            stopTextTimer()
            if (currentFrameText?.length == 1) {
                binding.textView.visibility = View.INVISIBLE
            }
            else {
                binding.textView.visibility = View.VISIBLE
                textAnimationDisposable = Observable.interval(0L, TEXT_ANIMATION_STEP, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            currentFrameText?.let { text ->
                                if (text.isNotEmpty()) {
                                    binding.textView.text = text.substring(0, currentFrameTextIndex + 1)
                                    if (currentFrameTextIndex < text.length - 1) currentFrameTextIndex++
                                    else stopTextTimer()
                                } else {
                                    stopTextTimer()
                                }
                            }
                        }
                currentFrameText?.let { frameText ->
                    if (frameText.isBlank()) binding.textView.setBackgroundColor(Color.TRANSPARENT)
                    else binding.textView.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.cutSceneTextFieldBackground))
                }
            }
        }
    }

    private fun takeNextScene(): Int {
        return if (decisionMade.isNotEmpty()) {
            when (gameState.getDecision(decisionMade)) {
                Common.GameState.Decision.YES -> yesNextScene
                Common.GameState.Decision.NO -> noNextScene
                else -> nextScene
            }
        } else nextScene
    }

    private fun showNextFrame() {
        if (isDecisionFrame) return

        if (currentFrameIndex == frames.size - 1 || isFinalFrame) {
            currentFrameIndex = 0
            val toScene = takeNextScene()
            gameState.setCutSceneShown(cutSceneId)
            findNavController().navigate(toScene)
        }
        else {
            val nextFrameIndex = frames[currentFrameIndex]?.nextFrame ?: DEFAULT_NEXT_FRAME
            if (nextFrameIndex == DEFAULT_NEXT_FRAME) {
                showFrame(++currentFrameIndex)
            }
            else {
                currentFrameIndex = nextFrameIndex
                showFrame(currentFrameIndex)
            }
        }
    }

    private fun makeDecision(value: Boolean) {
        frames[currentFrameIndex]?.let { frame ->
            gameState.setDecision(frame.decisionField, value)
            decisionMade = frame.decisionField
            currentFrameIndex = if (value) frame.yesNextFrame else frame.noNextFrame
            showFrame(currentFrameIndex)
        }
    }

    private fun showPreviousFrame() {
        if (currentFrameIndex == 0) return
        showFrame(--currentFrameIndex)
    }

    companion object {
        private const val TAG = "CutSceneFragment"
        private const val SCREEN_NAME = "Cut scene"

        const val CUT_SCENE_INFO = "cutSceneInfo"
        const val CUT_SCENE_ID = "id"
        const val BACKGROUND = "background"
        const val LEFT = "left"
        const val RIGHT = "right"
        const val TEXT = "text"
        const val FRAMES = "frames"
        const val NEXT_FRAME = "nextFrame"
        const val COLOR = "color"
        const val ANIMATION = "animation"
        const val ANIMATION_STEP = "animation_step"

        const val DECISION_FIELD = "decisionField"
        const val YES_NEXT_FRAME = "yesNextFrame"
        const val NO_NEXT_FRAME = "noNextFrame"
        const val SHOW_PREV_FRAME_BUTTON = "showPrevFrameButton"
        const val FINAL_FRAME = "finalFrame"

        const val DEFAULT_NEXT_FRAME = -1

        private const val TEXT_ANIMATION_STEP: Long = 30L
        private const val ANIMATION_FRAME_STEP: Long = 750L
    }

    private data class FrameData(val text: String,
                                 val background: String = "",
                                 val left: String = "",
                                 val right: String = "",
                                 val nextFrame: Int = DEFAULT_NEXT_FRAME,
                                 val decisionField: String = "",
                                 val yesNextFrame: Int = DEFAULT_NEXT_FRAME,
                                 val noNextFrame: Int = DEFAULT_NEXT_FRAME,
                                 val showPrevFrameButton: Boolean = false,
                                 val isFinalFrame: Boolean = false,
                                 val textColor: Int = 0,
                                 var animation: MutableList<Int>? = null,
                                 var animationStepMs: Long = ANIMATION_FRAME_STEP)
}
