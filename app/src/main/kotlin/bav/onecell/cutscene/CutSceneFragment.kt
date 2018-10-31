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
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_cut_scene.background
import kotlinx.android.synthetic.main.fragment_cut_scene.buttonNextFrame
import kotlinx.android.synthetic.main.fragment_cut_scene.buttonPreviousFrame
import kotlinx.android.synthetic.main.fragment_cut_scene.leftCharacter
import kotlinx.android.synthetic.main.fragment_cut_scene.rightCharacter
import kotlinx.android.synthetic.main.fragment_cut_scene.textView
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class CutSceneFragment : Fragment(), CutScene.View {

    @Inject lateinit var presenter: CutScene.Presenter
    @Inject lateinit var resourceProvider: Common.ResourceProvider
    private val disposables = CompositeDisposable()

    private var defaultBackground: Int = 0
    private var defaultLeftCharacter: Int = 0
    private var defaultRightCharacter: Int = 0
    private val frames: MutableList<FrameData> = mutableListOf()
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
                val framesArray = info.getJSONArray(FRAMES)
                for (i in 0 until framesArray.length()) {
                    val data = framesArray.getJSONObject(i)
                    frames.add(FrameData(data.optString(TEXT), data.optString(BACKGROUND),
                                         data.optString(LEFT), data.optString(RIGHT)))
                }
            } catch (e: JSONException) {
                Log.e(TAG, "wrong json: $e")
            }
        }
        showFrame(currentFrameIndex)
    }

    private fun showFrame(index: Int) {
        if (index !in 0 until frames.size) return
        val frameData = frames[index]
        background.setImageDrawable(ContextCompat.getDrawable(requireContext(), getBackground(frameData.background)))
        leftCharacter.setImageDrawable(ContextCompat.getDrawable(requireContext(), getLeftCharacter(frameData.left)))
        rightCharacter.setImageDrawable(ContextCompat.getDrawable(requireContext(), getRightCharacter(frameData.right)))
        textView.text = frameData.text
    }

    private fun showNextFrame() {
        if (currentFrameIndex == frames.size - 1) {
            findNavController().navigate(nextScene)
        }
        else {
            showFrame(++currentFrameIndex)
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
                                 val right: String = "")
}
