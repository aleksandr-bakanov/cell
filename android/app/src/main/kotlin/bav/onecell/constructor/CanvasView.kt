package bav.onecell.constructor

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import bav.onecell.model.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Layout
import bav.onecell.model.hexes.Orientation
import bav.onecell.model.hexes.Point
import java.lang.Math.max
import java.lang.Math.min
import android.util.AttributeSet

class CanvasView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    companion object {
        private val TAG = "CanvasView"
    }

    var backgroundFieldRadius: Int = 1
    var cell: Cell? = null

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

        /*
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
        )*/
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        isInitialized = false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawColor(Color.WHITE)

        initializeLayoutOrigin(canvas)
        drawBackgroundField(canvas)

        /*Log.d(TAG, "onDraw")

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
        }*/
    }

    private fun initializeLayoutOrigin(canvas: Canvas?) {
        if (!isInitialized) {
            layout.origin = Point(canvas!!.width.toDouble() / 2.0,
                    canvas.height.toDouble() / 2.0)
            isInitialized = true
        }
    }

    private fun drawBackgroundField(canvas: Canvas?) {
        val hexes: MutableSet<Hex> = mutableSetOf()
        for (q in -backgroundFieldRadius..backgroundFieldRadius) {
            val r1 = max(-backgroundFieldRadius, -q - backgroundFieldRadius)
            val r2 = min(backgroundFieldRadius, -q + backgroundFieldRadius)
            for (r in r1..r2) {
                hexes.add(Hex(q, r, -q - r))
            }
        }

        for (hex in hexes) {
            val hexCorners: ArrayList<Point> = Hex.poligonCorners(layout, hex)
            val points: ArrayList<Float> = arrayListOf()
            for (p in hexCorners) {
                points.add(p.x.toFloat())
                points.add(p.y.toFloat())
            }
            for (k in 0..(points.size / 2 - 1)) {
                if (k == (points.size / 2 - 1)) {
                    canvas?.drawLine(points[k * 2], points[k * 2 + 1], points[0], points[1], defaultPaint)
                } else {
                    canvas?.drawLine(points[k * 2], points[k * 2 + 1], points[(k + 1) * 2], points[(k + 1) * 2 + 1], defaultPaint)
                }
            }
        }
    }


}