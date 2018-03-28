package bav.onecell.constructor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import bav.onecell.common.view.CanvasView
import bav.onecell.model.Cell
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
                            val fHex = Hex.pixelToHex(layout, point)
                            val hex = Hex.hexRound(fHex)
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
        drawCell(canvas)
    }

    private fun drawCell(canvas: Canvas?) {
        cell?.let {
            var paint: Paint
            for (hex in it.hexes) {
                paint = when (hex.type) {
                    Hex.Type.LIFE -> lifePaint
                    Hex.Type.ENERGY -> energyPaint
                    Hex.Type.ATTACK -> attackPaint
                    else -> gridPaint
                }
                val path: Path = getHexPath(hex)
                path.fillType = Path.FillType.EVEN_ODD
                canvas?.drawPath(path, paint)
                canvas?.drawPath(path, strokePaint)
            }
        }
    }
}