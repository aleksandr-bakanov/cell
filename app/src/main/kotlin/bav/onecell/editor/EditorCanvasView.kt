package bav.onecell.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import bav.onecell.R
import bav.onecell.common.view.CanvasView
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Point

class EditorCanvasView(context: Context, attributeSet: AttributeSet) : CanvasView(context, attributeSet) {

    companion object {
        private val TAG = "EditorCanvasView"
    }

    var cell: Cell? = null
    lateinit var presenter: Editor.Presenter
    var selectedCellType: Hex.Type = Hex.Type.LIFE
    var tipHexes: Collection<Hex>? = null
    private val tipPaint = Paint()

    init {
        tipPaint.style = Paint.Style.FILL
        tipPaint.color = ContextCompat.getColor(context, R.color.cellEditorTip)

        setOnTouchListener { view: View?, event: MotionEvent? ->
            super.onTouchListener(view, event)
            if (event?.action == MotionEvent.ACTION_UP) {
                if (event.pointerCount == 1) {
                    val hex = pointToHex(event.x, event.y)
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
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        cell?.let {
            it.evaluateCellHexesPower()
            drawUtils.drawHexes(canvas, it.data.origin, tipHexes, tipPaint, layout)
            drawUtils.drawCell(canvas, it, layout = layout)
            drawUtils.drawCellPower(canvas, it, layout)
        }
    }

    fun pointToHex(x: Float, y: Float): Hex {
        val point = Point(x.toDouble(), y.toDouble())
        val fHex = hexMath.pixelToHex(layout, point)
        return hexMath.round(fHex)
    }
}
