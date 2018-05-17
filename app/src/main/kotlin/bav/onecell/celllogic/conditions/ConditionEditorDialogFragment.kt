package bav.onecell.celllogic.conditions

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.celllogic.CellLogicModule
import bav.onecell.model.cell.logic.Condition
import javax.inject.Inject

class ConditionEditorDialogFragment : DialogFragment(), ConditionEditor.View {

    @Inject
    lateinit var presenter: ConditionEditor.Presenter
    lateinit var condition: Condition

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        inject()
        presenter.initialize(condition, arguments.getInt(WHAT_TO_EDIT))

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
        (activity.application as OneCellApplication).appComponent
                .plus(CellLogicModule())
                .inject(this)
    }

    fun with(c: Condition): ConditionEditorDialogFragment {
        condition = c
        return this
    }

    companion object {
        private const val WHAT_TO_EDIT = "what_to_edit"

        @JvmStatic
        fun newInstance(condition: Condition, whatToEdit: Int): ConditionEditorDialogFragment {
            val fragment = ConditionEditorDialogFragment().with(condition)
            val bundle = Bundle()
            bundle.putInt(WHAT_TO_EDIT, whatToEdit)
            fragment.arguments = bundle
            return fragment
        }
    }
}
