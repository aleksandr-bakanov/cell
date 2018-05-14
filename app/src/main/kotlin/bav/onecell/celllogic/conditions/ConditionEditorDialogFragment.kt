package bav.onecell.celllogic.conditions

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import bav.onecell.R
import bav.onecell.celllogic.CellLogic

class ConditionEditorDialogFragment : DialogFragment() {

    private lateinit var host: CellLogic.PresenterProvider

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (activity is CellLogic.PresenterProvider) {
            host = activity
        } else {
            throw RuntimeException("$activity must implement CellLogic.PresenterProvider")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CellLogic.PresenterProvider) {
            host = context
        } else {
            throw RuntimeException(context.toString() + " must implement CellLogic.PresenterProvider")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder
                .setItems(host.provideCellLogicPresenter().provideConditionDialogValues(), { dialog, which ->
                    host.provideCellLogicPresenter().saveConditionValue(which)
                })
                .setPositiveButton(R.string.button_ok, { dialog, which ->
                    host.provideCellLogicPresenter().saveCondition()
                })
                .setNegativeButton(R.string.button_cancel, { dialog, which ->

                })
        return builder.create()
    }
}
