package bav.onecell.celllogic

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import bav.onecell.R

class ConditionEditorDialogFragment : DialogFragment() {

    private lateinit var host: CellLogic.PresenterProvider

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Log.d("ConditionEditorDialog", "onAttach $activity")
        if (activity is CellLogic.PresenterProvider) {
            host = activity
        } else {
            throw RuntimeException("$activity must implement CellLogic.PresenterProvider")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("ConditionEditorDialog", "onAttach $context")
        if (context is CellLogic.PresenterProvider) {
            host = context
        } else {
            throw RuntimeException(context.toString() + " must implement CellLogic.PresenterProvider")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d("ConditionEditorDialog", "onCreateDialog")
        val builder = AlertDialog.Builder(activity)
        builder
            .setItems(host.provideCellLogicPresenter().provideConditionDialogValues(), { dialog, which ->

            })
            .setPositiveButton(R.string.button_ok, { dialog, which ->
                host.provideCellLogicPresenter().saveCondition()
            })
            .setNegativeButton(R.string.button_cancel, { dialog, which ->

            })
        return builder.create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d("ConditionEditorDialog", "onActivityCreated")
    }
}
