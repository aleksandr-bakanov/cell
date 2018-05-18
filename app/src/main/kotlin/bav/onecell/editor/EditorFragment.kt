package bav.onecell.editor

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.OneCellApplication
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
import javax.inject.Inject

class EditorFragment : Fragment(), Editor.View {

    @Inject
    lateinit var presenter: Editor.Presenter
    @Inject
    lateinit var hexMath: HexMath

    // TODO: same variable exists in EditorCanvasView, it is unnecessary duplicate
    private var selectedCellType: Hex.Type = Hex.Type.LIFE
    private val disposables = CompositeDisposable()

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "$this onCreateView")
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "$this onActivityCreated")

        inject()

        editorCanvasView.hexMath = hexMath
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
                },
                presenter.getBackgroundCellRadiusProvider().subscribe {
                    editorCanvasView.backgroundFieldRadius = it
                    editorCanvasView.invalidate()
                }
        )
        arguments?.let { presenter.initialize(it.getInt(CELL_INDEX)) }
    }

    override fun onDestroyView() {
        Log.d(TAG, "$this onDestroyView")
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
            R.id.buttonRotateCellLeft -> presenter.rotateCellLeft()
            R.id.buttonRotateCellRight -> presenter.rotateCellRight()
        }
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, "$this onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "$this onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "$this onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "$this onResume")
    }

    override fun onPause() {
        Log.d(TAG, "$this onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "$this onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "$this onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG, "$this onDetach")
        super.onDetach()
    }
}
