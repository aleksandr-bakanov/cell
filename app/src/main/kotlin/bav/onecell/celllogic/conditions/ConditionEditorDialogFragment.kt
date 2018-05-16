package bav.onecell.celllogic.conditions

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.celllogic.CellLogicModule
import javax.inject.Inject

class ConditionEditorDialogFragment : DialogFragment() {

    @Inject
    lateinit var presenter: Conditions.Presenter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        inject()
        presenter.initialize(arguments.getInt(CELL_INDEX), arguments.getInt(RULE_INDEX))

        val builder = AlertDialog.Builder(activity)
        builder
                .setItems(presenter.provideConditionDialogValues(), { dialog, which ->
                    presenter.saveConditionValue(which)
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
        private const val RULE_INDEX = "rule_index"

        @JvmStatic
        fun newInstance(cellIndex: Int, ruleIndex: Int): ConditionEditorDialogFragment {
            val bundle = Bundle()
            bundle.putInt(CELL_INDEX, cellIndex)
            bundle.putInt(RULE_INDEX, ruleIndex)
            val fragment = ConditionEditorDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
