package bav.onecell.common.router

import android.content.Context
import android.content.Intent
import bav.onecell.constructor.ConstructorActivity

class RouterImpl(private val context: Context) : Router {
    override fun goToCellConstructor(cellIndex: Int) {
        val intent = ConstructorActivity.newIntent(context, cellIndex)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}