/*
 * Copyright (c) 2018 Disney ABC Television Group
 *
 * cell
 * ActionEditorDialogFragment.kt
 *
 * Author: Aleksandr Bakanov <aleksandr_bakanov@epam.com>
 * Date:   May 08, 2018
 */

package bav.onecell.celllogic

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import bav.onecell.R

class ActionEditorDialogFragment : DialogFragment() {

    private lateinit var host: CellLogic.PresenterProvider

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Log.d("ActionEditorDialog", "onAttach $activity")
        if (activity is CellLogic.PresenterProvider) {
            host = activity
        } else {
            throw RuntimeException("$activity must implement CellLogic.PresenterProvider")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("ActionEditorDialog", "onAttach $context")
        if (context is CellLogic.PresenterProvider) {
            host = context
        } else {
            throw RuntimeException(context.toString() + " must implement CellLogic.PresenterProvider")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d("ActionEditorDialog", "onCreateDialog")
        val builder = AlertDialog.Builder(activity)
        builder
                .setItems(host.provideCellLogicPresenter().provideActionDialogValues(), { dialog, which ->
                    host.provideCellLogicPresenter().saveActionValue(which)
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
        Log.d("ActionEditorDialog", "onActivityCreated")
    }
}
