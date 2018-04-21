package bav.onecell.constructor

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import bav.onecell.common.view.CanvasView
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Point

class ConstructorCanvasView(context: Context, attributeSet: AttributeSet) : CanvasView(context, attributeSet) {

    companion object {
        private val TAG = "ConstructorCanvasView"
    }

    var cell: Cell? = null
    lateinit var presenter: Constructor.Presenter
    var selectedCellType: Hex.Type = Hex.Type.LIFE

    init {
        setOnTouchListener(
                { view: View?, event: MotionEvent? ->
                    super.onTouchListener(view, event)
                    if (event?.action == MotionEvent.ACTION_UP) {
                        if (event.pointerCount == 1) {
                            val x: Double = event.x.toDouble()
                            val y: Double = event.y.toDouble()
                            val point = Point(x, y)
                            val fHex = hexMath.pixelToHex(layout, point)
                            val hex = hexMath.round(fHex)
                            if (selectedCellType == Hex.Type.REMOVE) {
                                presenter.removeHexFromCell(hex)
                            } else {
                                hex.type = selectedCellType
                                presenter.addHexToCell(hex)
                            }
                            invalidate()
                        }
                    }
                    true
                }
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawCell(canvas, cell)
    }
}
