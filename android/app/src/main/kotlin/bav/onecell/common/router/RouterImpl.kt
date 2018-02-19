package bav.onecell.common.router

import android.content.Context
import bav.onecell.constructor.ConstructorActivity

class RouterImpl(private val context: Context) : Router {
    override fun goToCellConstructor(cellIndex: Int) {
        val intent = ConstructorActivity.newIntent(context, cellIndex)
        context.startActivity(intent)
    }
}