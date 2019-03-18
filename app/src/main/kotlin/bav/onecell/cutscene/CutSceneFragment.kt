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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_cut_scene.background
import kotlinx.android.synthetic.main.fragment_cut_scene.buttonNo
import kotlinx.android.synthetic.main.fragment_cut_scene.buttonPreviousFrame
import kotlinx.android.synthetic.main.fragment_cut_scene.buttonYes
import kotlinx.android.synthetic.main.fragment_cut_scene.leftCharacter
import kotlinx.android.synthetic.main.fragment_cut_scene.rightCharacter
import kotlinx.android.synthetic.main.fragment_cut_scene.textView
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

    private var animationTimer: Disposable? = null
    private var currentFrameTextIndex: Int = 0
    private var currentFrameText: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cut_scene, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        background.setOnClickListener { showNextFrame() }
        buttonPreviousFrame.setOnClickListener { showPreviousFrame() }
        buttonYes.setOnClickListener { makeDecision(true) }
        buttonNo.setOnClickListener { makeDecision(false) }
        textView.movementMethod = ScrollingMovementMethod()

        parseArguments(arguments)
    }

    override fun onStart() {
        super.onStart()
        if (!gameState.getIgnoreCutSceneShownStatus() && gameState.isCutSceneAlreadyShown(cutSceneId)) {
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
        super.onPause()
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    private fun stopTextTimer() {
        animationTimer?.let { if (!it.isDisposed) it.dispose() }
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
                    frames[i.toInt()] = FrameData(text = data.optString(TEXT), background = data.optString(BACKGROUND),
                                          left = data.optString(LEFT), right = data.optString(RIGHT),
                                          nextFrame = data.optInt(NEXT_FRAME, DEFAULT_NEXT_FRAME),
                                          decisionField = data.optString(DECISION_FIELD),
                                          yesNextFrame = data.optInt(YES_NEXT_FRAME, DEFAULT_NEXT_FRAME),
                                          noNextFrame = data.optInt(NO_NEXT_FRAME, DEFAULT_NEXT_FRAME),
                                          showPrevFrameButton = data.optBoolean(SHOW_PREV_FRAME_BUTTON, false),
                                          isFinalFrame = data.optBoolean(FINAL_FRAME, false),
                                          textColor = resourceProvider.getColor(data.optString(COLOR)))
                }
                info.optJSONObject(GAME_STATE_CHANGES)?.let { gameStateChanges ->
                    // Changes should contain booleans
                    for (decision in gameStateChanges.keys()) {
                        gameState.setDecision(decision, gameStateChanges.getBoolean(decision))
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG, "wrong json: $e")
            }
        }
    }

    private fun showFrame(index: Int) {
        Log.d(TAG, "showFrame($index)")
        frames[index]?.let {
            background.setImageDrawable(ContextCompat.getDrawable(requireContext(), getBackground(it.background)))
            leftCharacter.setImageDrawable(ContextCompat.getDrawable(requireContext(), getLeftCharacter(it.left)))
            rightCharacter.setImageDrawable(ContextCompat.getDrawable(requireContext(), getRightCharacter(it.right)))

            textView.setTextColor(it.textColor)

            // TODO: don't give a choice if decision has been taken already
            buttonYes.visible = it.decisionField.isNotEmpty()
            buttonNo.visible = it.decisionField.isNotEmpty()
            isDecisionFrame = it.decisionField.isNotEmpty()
            isFinalFrame = it.isFinalFrame

            buttonPreviousFrame.visibility = if (it.showPrevFrameButton) View.VISIBLE else View.GONE

            currentFrameText = resourceProvider.getString(it.text)
            currentFrameTextIndex = 0
            stopTextTimer()
            if (currentFrameText?.length == 1) {
                textView.visibility = View.INVISIBLE
            }
            else {
                textView.visibility = View.VISIBLE
                animationTimer = Observable.interval(0L, TEXT_ANIMATION_STEP, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            currentFrameText?.let { text ->
                                if (text.isNotEmpty()) {
                                    textView.text = text.substring(0, currentFrameTextIndex + 1)
                                    if (currentFrameTextIndex < text.length - 1) currentFrameTextIndex++
                                    else stopTextTimer()
                                } else {
                                    stopTextTimer()
                                }
                            }
                        }
                currentFrameText?.let { frameText ->
                    if (frameText.isBlank()) textView.setBackgroundColor(Color.TRANSPARENT)
                    else textView.setBackgroundColor(
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

        const val DECISION_FIELD = "decisionField"
        const val YES_NEXT_FRAME = "yesNextFrame"
        const val NO_NEXT_FRAME = "noNextFrame"
        const val SHOW_PREV_FRAME_BUTTON = "showPrevFrameButton"
        const val FINAL_FRAME = "finalFrame"

        const val DEFAULT_NEXT_FRAME = -1

        private const val TEXT_ANIMATION_STEP: Long = 30L

        @JvmStatic
        fun newInstance(bundle: Bundle?): CutSceneFragment {
            val fragment = CutSceneFragment()
            fragment.arguments = bundle
            return fragment
        }
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
                                 val textColor: Int = 0)
}
