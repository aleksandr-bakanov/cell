package bav.onecell.editor

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_editor.buttonRotateCellLeft
import kotlinx.android.synthetic.main.fragment_editor.buttonRotateCellRight
import kotlinx.android.synthetic.main.fragment_editor.editorCanvasView
import kotlinx.android.synthetic.main.fragment_editor.radioButtonAttackCell
import kotlinx.android.synthetic.main.fragment_editor.radioButtonEnergyCell
import kotlinx.android.synthetic.main.fragment_editor.radioButtonLifeCell
import kotlinx.android.synthetic.main.fragment_editor.radioButtonRemoveCell
import kotlinx.android.synthetic.main.fragment_editor.textMoney
import javax.inject.Inject
import kotlin.math.PI

class EditorFragment : Fragment(), Editor.View {

    @Inject lateinit var presenter: Editor.Presenter
    @Inject lateinit var hexMath: HexMath
    @Inject lateinit var drawUtils: DrawUtils

    private val disposables = CompositeDisposable()

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        editorCanvasView.hexMath = hexMath
        editorCanvasView.drawUtils = drawUtils
        editorCanvasView.presenter = presenter

        radioButtonLifeCell.setOnClickListener { onCellTypeRadioButtonClicked(it) }
        radioButtonAttackCell.setOnClickListener { onCellTypeRadioButtonClicked(it) }
        radioButtonEnergyCell.setOnClickListener { onCellTypeRadioButtonClicked(it) }
        radioButtonRemoveCell.setOnClickListener { onCellTypeRadioButtonClicked(it) }

        buttonRotateCellLeft.setOnClickListener { onCellRotateButtonClicked(it) }
        buttonRotateCellRight.setOnClickListener { onCellRotateButtonClicked(it) }

        disposables.addAll(
                presenter.getCellProvider().subscribe {
                    editorCanvasView.cell = it
                    textMoney.text = resources.getString(R.string.text_money, it.data.money)

                    disposables.add(it.getMoneyProvider().subscribe { money ->
                        textMoney.text = resources.getString(R.string.text_money, money)
                    })
                },
                presenter.getBackgroundCellRadiusProvider().subscribe {
                    editorCanvasView.backgroundFieldRadius = it
                    editorCanvasView.invalidate()
                }
        )
        arguments?.let { presenter.initialize(it.getInt(CELL_INDEX)) }
        highlightTips(editorCanvasView.selectedCellType)
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }
    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(EditorModule(this))
                .inject(this)
    }
    //endregion

    //region View listeners
    private fun onCellTypeRadioButtonClicked(view: View) {
        val type = when (view.id) {
            R.id.radioButtonLifeCell -> Hex.Type.LIFE
            R.id.radioButtonEnergyCell -> Hex.Type.ENERGY
            R.id.radioButtonAttackCell -> Hex.Type.ATTACK
            else -> Hex.Type.REMOVE
        }
        editorCanvasView.selectedCellType = type
        editorCanvasView.tipHexes = presenter.getTipHexes(type)
        editorCanvasView.invalidate()
    }

    private fun onCellRotateButtonClicked(view: View) {
        when (view.id) {
            R.id.buttonRotateCellLeft -> {
                //presenter.rotateCellLeft()
                editorCanvasView.tipHexes = null
                animateCellRotationLeft()
            }
            R.id.buttonRotateCellRight -> {
                //presenter.rotateCellRight()
                editorCanvasView.tipHexes = null
                animateCellRotationRight()
            }
        }
    }

    private fun animateCellRotationLeft() {
        ValueAnimator.ofFloat(0f, -PI.toFloat() / 3f).apply {
            duration = 1000
            addUpdateListener {
                editorCanvasView.cell?.animationData?.rotation = it.animatedValue as Float
                editorCanvasView.invalidate()
            }
            addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    editorCanvasView.cell?.animationData?.rotation = 0f
                    presenter.rotateCellLeft()
                    editorCanvasView.tipHexes = presenter.getTipHexes(editorCanvasView.selectedCellType)
                    editorCanvasView.invalidate()
                }
            })
            start()
        }
    }

    private fun animateCellRotationRight() {
        ValueAnimator.ofFloat(0f, PI.toFloat() / 3f).apply {
            duration = 1000
            addUpdateListener {
                editorCanvasView.cell?.animationData?.rotation = it.animatedValue as Float
                editorCanvasView.invalidate()
            }
            addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    editorCanvasView.cell?.animationData?.rotation = 0f
                    presenter.rotateCellRight()
                    editorCanvasView.tipHexes = presenter.getTipHexes(editorCanvasView.selectedCellType)
                    editorCanvasView.invalidate()
                }
            })
            start()
        }
    }
    //endregion

    //region Overridden methods
    override fun highlightTips(type: Hex.Type) {
        editorCanvasView.tipHexes = presenter.getTipHexes(type)
        editorCanvasView.invalidate()
    }
    //endregion

    companion object {
        private const val TAG = "EditorFragment"
        const val CELL_INDEX = "cell_index"

        /**
         * Creates new instance of fragment
         *
         * @param index Index of cell to be edited
         */
        @JvmStatic
        fun newInstance(bundle: Bundle?): EditorFragment {
            val fragment = EditorFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
