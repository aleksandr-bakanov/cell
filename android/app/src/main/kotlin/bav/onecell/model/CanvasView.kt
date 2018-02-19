package bav.onecell.model

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Layout
import bav.onecell.model.hexes.Orientation
import bav.onecell.model.hexes.Point

class CanvasView(context: Context, private val hexes: MutableSet<Hex>) : View(context) {

    companion object {
        private val TAG = "CanvasView"
    }

    private val layout = Layout(
            Orientation.LAYOUT_POINTY,
            Point(100.0, 100.0), Point(100.0, 100.0))
    private val defaultPaint: Paint = Paint()
    private val filledPaint: Paint = Paint()
    private var isInitialized = false
    private var touchedHex: Hex? = null

    init {
        defaultPaint.color = Color.BLACK
        defaultPaint.strokeWidth = 5.0f

        filledPaint.style = Paint.Style.FILL
        filledPaint.color = Color.RED
        filledPaint.strokeWidth = 10.0f

        setOnTouchListener(
                { view: View?, event: MotionEvent? ->
                    if (event?.action == MotionEvent.ACTION_UP) {
                        val x: Double = event.x.toDouble()
                        val y: Double = event.y.toDouble()
                        val point = Point(x, y)
                        val fHex = Hex.pixelToHex(layout, point)
                        val hex = Hex.hexRound(fHex)
                        if (hexes.contains(hex)) {
                            touchedHex = hex
                        } else {
                            touchedHex = null
                        }
                        Log.d(TAG, "point = $point; touchedHex = $touchedHex")
                        invalidate()
                    }
                    true
                }
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        isInitialized = false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        Log.d(TAG, "onDraw")

        if (!isInitialized) {
            layout.origin = Point(canvas!!.width.toDouble() / 2.0,
                    canvas.height.toDouble() / 2.0)
            isInitialized = true
        }

        canvas?.drawColor(Color.WHITE)

        var paint: Paint
        for (hex in hexes) {
            if (touchedHex != null && hex == touchedHex) {
                paint = filledPaint
            } else {
                paint = defaultPaint
            }
            val hexCorners: ArrayList<Point> = Hex.poligonCorners(layout, hex)
            val points: ArrayList<Float> = arrayListOf()
            for (p in hexCorners) {
                points.add(p.x.toFloat())
                points.add(p.y.toFloat())
            }
            for (k in 0..(points.size / 2 - 1)) {
                if (k == (points.size / 2 - 1)) {
                    canvas?.drawLine(points[k * 2], points[k * 2 + 1], points[0], points[1], paint)
                } else {
                    canvas?.drawLine(points[k * 2], points[k * 2 + 1], points[(k + 1) * 2], points[(k + 1) * 2 + 1], paint)
                }
            }
        }
    }
}