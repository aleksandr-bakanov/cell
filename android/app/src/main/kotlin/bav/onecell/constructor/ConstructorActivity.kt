package bav.onecell.constructor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

class ConstructorActivity: Activity(), Constructor.View {
    companion object {
        private val EXTRA_CELL_INDEX = "extra_cell_index"

        fun newIntent(context: Context, cellIndex: Int): Intent {
            val extras = Bundle()
            extras.putInt(EXTRA_CELL_INDEX, cellIndex)
            val intent = Intent(context, ConstructorActivity::class.java)
            intent.putExtras(extras)
            return intent
        }
    }
}