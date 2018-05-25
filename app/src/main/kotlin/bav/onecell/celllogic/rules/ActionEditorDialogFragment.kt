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
    lateinit var presenter: ActionEditor.Presenter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        inject()
        presenter.initialize(arguments.getInt(CELL_INDEX), arguments.getInt(RULE_INDEX))

        val builder = AlertDialog.Builder(activity)
        builder
                .setItems(presenter.provideActionDialogValues(), { _, which ->
                    presenter.saveActionValue(which)
                })
                .setPositiveButton(R.string.button_ok, { _, _ -> })
                .setNegativeButton(R.string.button_cancel, { _, _ -> })
        return builder.create()
    }

    private fun inject() {
        (activity.application as OneCellApplication).appComponent.plus(CellLogicModule()).inject(this)
    }

    companion object {
        const val ACTION_EDITOR_DIALOG_TAG = "action_editor_dialog"
        const val CELL_INDEX = "cell_index"
        const val RULE_INDEX = "rule_index"

        @JvmStatic
        fun newInstance(cellIndex: Int, ruleIndex: Int): ActionEditorDialogFragment {
            val bundle = Bundle()
            bundle.putInt(CELL_INDEX, cellIndex)
            bundle.putInt(RULE_INDEX, ruleIndex)
            val fragment = ActionEditorDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
