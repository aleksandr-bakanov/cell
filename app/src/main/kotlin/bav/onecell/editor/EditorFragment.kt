package bav.onecell.editor

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
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

class EditorFragment : Fragment() {

    private lateinit var listener: OnEditorFragmentInteractionListener
    // TODO: same variable exists in EditorCanvasView, it is unnecessary duplicate
    private var selectedCellType: Hex.Type = Hex.Type.LIFE
    private val disposables = CompositeDisposable()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnEditorFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnEditorFragmentInteractionListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        editorCanvasView.hexMath = listener.provideHexMath()
        editorCanvasView.mPresenter = listener.provideEditorPresenter()
        
        radioButtonLifeCell.setOnClickListener { onCellTypeRadioButtonClicked(it) }
        radioButtonAttackCell.setOnClickListener { onCellTypeRadioButtonClicked(it) }
        radioButtonEnergyCell.setOnClickListener { onCellTypeRadioButtonClicked(it) }
        radioButtonRemoveCell.setOnClickListener { onCellTypeRadioButtonClicked(it) }
        
        buttonRotateCellLeft.setOnClickListener { onCellRotateButtonClicked(it) }
        buttonRotateCellRight.setOnClickListener { onCellRotateButtonClicked(it) }

        // TODO: manage disposable
        with (listener.provideEditorPresenter()) {
            disposables.add(getCellProvider().subscribe {
                editorCanvasView.cell = it
            })
            disposables.add(getBackgroundCellRadiusProvider().subscribe {
                editorCanvasView.backgroundFieldRadius = it
                editorCanvasView.invalidate()
            })
        }
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    //region View listeners
    private fun onCellTypeRadioButtonClicked(view: View) {
        selectedCellType = when (view.id) {
            R.id.radioButtonLifeCell -> Hex.Type.LIFE
            R.id.radioButtonEnergyCell -> Hex.Type.ENERGY
            R.id.radioButtonAttackCell -> Hex.Type.ATTACK
            else -> Hex.Type.REMOVE
        }
        editorCanvasView.selectedCellType = selectedCellType
    }

    private fun onCellRotateButtonClicked(view: View) {
        when (view.id) {
            R.id.buttonRotateCellLeft -> listener.provideEditorPresenter().rotateCellLeft()
            R.id.buttonRotateCellRight -> listener.provideEditorPresenter().rotateCellRight()
        }
        editorCanvasView.invalidate()
    }
    //endregion

    interface OnEditorFragmentInteractionListener {
        fun provideEditorPresenter(): Editor.Presenter
        fun provideHexMath(): HexMath
    }
}