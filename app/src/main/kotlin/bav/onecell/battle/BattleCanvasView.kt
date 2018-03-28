package bav.onecell.battle

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import bav.onecell.common.view.CanvasView

class BattleCanvasView(context: Context, attributeSet: AttributeSet) : CanvasView(context, attributeSet) {

    companion object {
        private val TAG = "BattleCanvasView"
    }

    lateinit var presenter: Battle.Presenter

    init {
        setOnTouchListener(
                { view: View?, event: MotionEvent? ->
                    super.onTouchListener(view, event)
                }
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

}