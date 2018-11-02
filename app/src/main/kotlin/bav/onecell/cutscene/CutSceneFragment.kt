package bav.onecell.cutscene

import android.os.Bundle
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
import bav.onecell.common.extensions.visible
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_cut_scene.background
import kotlinx.android.synthetic.main.fragment_cut_scene.buttonNextFrame
import kotlinx.android.synthetic.main.fragment_cut_scene.buttonNo
import kotlinx.android.synthetic.main.fragment_cut_scene.buttonPreviousFrame
import kotlinx.android.synthetic.main.fragment_cut_scene.buttonYes
import kotlinx.android.synthetic.main.fragment_cut_scene.leftCharacter
import kotlinx.android.synthetic.main.fragment_cut_scene.rightCharacter
import kotlinx.android.synthetic.main.fragment_cut_scene.textView
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class CutSceneFragment : Fragment(), CutScene.View {

    @Inject lateinit var presenter: CutScene.Presenter
    @Inject lateinit var resourceProvider: Common.ResourceProvider
    @Inject lateinit var gameState: Common.GameState
    private val disposables = CompositeDisposable()

    private var defaultBackground: Int = 0
    private var defaultLeftCharacter: Int = 0
    private var defaultRightCharacter: Int = 0
    private val frames: MutableMap<Int, FrameData> = mutableMapOf()
    private var nextScene: Int = 0
    private var currentFrameIndex: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cut_scene, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        buttonNextFrame.setOnClickListener { showNextFrame() }
        buttonPreviousFrame.setOnClickListener { showPreviousFrame() }
        buttonYes.setOnClickListener { makeDecision(true) }
        buttonNo.setOnClickListener { makeDecision(false) }

        parseArguments(arguments)
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
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
                val info = JSONObject(it.getString(CUT_SCENE_INFO))
                defaultBackground = resourceProvider.getDrawableIdentifier(info.getString(BACKGROUND))
                defaultLeftCharacter = resourceProvider.getDrawableIdentifier(info.getString(LEFT))
                defaultRightCharacter = resourceProvider.getDrawableIdentifier(info.getString(RIGHT))
                nextScene = resourceProvider.getIdIdentifier(info.getString(Consts.NEXT_SCENE))
                val framesMap = info.getJSONObject(FRAMES)
                for (i in framesMap.keys()) {
                    val data = framesMap.getJSONObject(i)
                    frames[i.toInt()] = FrameData(text = data.optString(TEXT), background = data.optString(BACKGROUND),
                                          left = data.optString(LEFT), right = data.optString(RIGHT),
                                          nextFrame = data.optInt(NEXT_FRAME, DEFAULT_NEXT_FRAME),
                                          decisionField = data.optString(DECISION_FIELD),
                                          yesNextFrame = data.optInt(YES_NEXT_FRAME, DEFAULT_NEXT_FRAME),
                                          noNextFrame = data.optInt(NO_NEXT_FRAME, DEFAULT_NEXT_FRAME))
                }
            } catch (e: JSONException) {
                Log.e(TAG, "wrong json: $e")
            }
        }
        showFrame(currentFrameIndex)
    }

    private fun showFrame(index: Int) {
        frames[index]?.let {
            background.setImageDrawable(ContextCompat.getDrawable(requireContext(), getBackground(it.background)))
            leftCharacter.setImageDrawable(ContextCompat.getDrawable(requireContext(), getLeftCharacter(it.left)))
            rightCharacter.setImageDrawable(ContextCompat.getDrawable(requireContext(), getRightCharacter(it.right)))
            textView.text = resourceProvider.getString(it.text)

            // TODO: don't give a choice if decision has been taken already
            buttonNextFrame.visible = it.decisionField.isEmpty()
            buttonPreviousFrame.visible = it.decisionField.isEmpty()
            buttonYes.visible = it.decisionField.isNotEmpty()
            buttonNo.visible = it.decisionField.isNotEmpty()
        }
    }

    private fun showNextFrame() {
        if (currentFrameIndex == frames.size - 1) {
            findNavController().navigate(nextScene)
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

        const val CUT_SCENE_INFO = "cutSceneInfo"
        const val BACKGROUND = "background"
        const val LEFT = "left"
        const val RIGHT = "right"
        const val TEXT = "text"
        const val FRAMES = "frames"
        const val NEXT_FRAME = "nextFrame"

        const val DECISION_FIELD = "decisionField"
        const val YES_NEXT_FRAME = "yesNextFrame"
        const val NO_NEXT_FRAME = "noNextFrame"

        const val DEFAULT_NEXT_FRAME = -1

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
                                 val noNextFrame: Int = DEFAULT_NEXT_FRAME)
}
