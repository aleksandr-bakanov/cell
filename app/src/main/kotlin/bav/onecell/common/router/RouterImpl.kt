package bav.onecell.common.router

import android.content.Context
import bav.onecell.battle.BattleActivity
import bav.onecell.celllogic.CellLogicActivity

class RouterImpl(private val appContext: Context) : Router {
    override fun goToCellLogicEditor(context: Context, cellIndex: Int) {
        val intent = CellLogicActivity.newIntent(context, cellIndex)
        context.startActivity(intent)
    }

    override fun goToBattleView(context: Context, cellIndexes: List<Int>) {
        val intent = BattleActivity.newIntent(context, cellIndexes)
        context.startActivity(intent)
    }
}