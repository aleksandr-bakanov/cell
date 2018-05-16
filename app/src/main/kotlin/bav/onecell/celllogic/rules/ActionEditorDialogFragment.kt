package bav.onecell.celllogic.rules

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.celllogic.CellLogicModule
import javax.inject.Inject

class ActionEditorDialogFragment : DialogFragment() {

    @Inject
    lateinit var presenter: Rules.Presenter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        inject()
        presenter.initialize(arguments.getInt(CELL_INDEX))

        val builder = AlertDialog.Builder(activity)
        builder
                .setItems(presenter.provideActionDialogValues(), { dialog, which ->
                    presenter.saveActionValue(which)
                })
                .setPositiveButton(R.string.button_ok, { dialog, which ->

                })
                .setNegativeButton(R.string.button_cancel, { dialog, which ->

                })
        return builder.create()
    }

    private fun inject() {
        (activity.application as OneCellApplication).appComponent.plus(CellLogicModule()).inject(this)
    }

    companion object {
        private const val CELL_INDEX = "cell_index"

        @JvmStatic
        fun newInstance(index: Int): ActionEditorDialogFragment {
            val bundle = Bundle()
            bundle.putInt(CELL_INDEX, index)
            val fragment = ActionEditorDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
