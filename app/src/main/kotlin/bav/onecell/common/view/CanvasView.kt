package bav.onecell.common.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import bav.onecell.R
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Layout
import bav.onecell.model.hexes.Orientation
import bav.onecell.model.hexes.Point

open class CanvasView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    var backgroundFieldRadius: Int = 1
        set(value) {
            backgroundHexes.clear()
            for (q in -value..value) {
                val r1 = Math.max(-value, -q - value)
                val r2 = Math.min(value, -q + value)
                for (r in r1..r2) {
                    backgroundHexes.add(Hex(q, r, -q - r))
                }
            }
            invalidate()
        }

    private val layoutHexSize = Point(75.0, 75.0)
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    protected val layout = Layout(
            Orientation.LAYOUT_POINTY, layoutHexSize, Point())

    protected val gridPaint: Paint = Paint()
    protected val lifePaint: Paint = Paint()
    protected val energyPaint: Paint = Paint()
    protected val attackPaint: Paint = Paint()
    protected val strokePaint: Paint = Paint()
    private val darkStrokePaint: Paint = Paint()
    private val lightStrokePaint: Paint = Paint()
    private val coordinateTextPaint: Paint = Paint()
    private val coordinateTextVerticalOffset = (layoutHexSize.x / 10).toFloat()

    private var isInitialized = false
    private val backgroundHexes: MutableSet<Hex> = mutableSetOf()

    init {
        gridPaint.style = Paint.Style.STROKE
        gridPaint.color = ContextCompat.getColor(context, R.color.cellConstructorGrid)
        gridPaint.strokeWidth = 5.0f

        lifePaint.style = Paint.Style.FILL
        lifePaint.color = ContextCompat.getColor(context, R.color.cellConstructorLife)

        energyPaint.style = Paint.Style.FILL
        energyPaint.color = ContextCompat.getColor(context, R.color.cellConstructorEnergy)

        attackPaint.style = Paint.Style.FILL
        attackPaint.color = ContextCompat.getColor(context, R.color.cellConstructorAttack)

        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = ContextCompat.getColor(context, R.color.cellConstructorStroke)
        strokePaint.strokeWidth = 5.0f

        darkStrokePaint.style = Paint.Style.STROKE
        darkStrokePaint.color = Color.BLACK
        darkStrokePaint.strokeWidth = 5.0f

        lightStrokePaint.style = Paint.Style.STROKE
        lightStrokePaint.color = Color.WHITE
        lightStrokePaint.strokeWidth = 5.0f

        coordinateTextPaint.color = Color.BLACK
        coordinateTextPaint.textSize = 48f
        coordinateTextPaint.textAlign = Paint.Align.CENTER
    }

    protected fun onTouchListener(view: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            lastTouchX = event.getX(event.getPointerId(0))
            lastTouchY = event.getY(event.getPointerId(0))
        } else if (event?.action == MotionEvent.ACTION_MOVE) {
            if (event.pointerCount > 1) {
                val curX = event.getX(event.getPointerId(0))
                val curY = event.getY(event.getPointerId(0))
                val dx = curX - lastTouchX
                val dy = curY - lastTouchY
                lastTouchX = curX
                lastTouchY = curY
                layout.origin = Point(layout.origin.x + dx, layout.origin.y + dy)
                invalidate()
            }
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        isInitialized = false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(ContextCompat.getColor(context, R.color.cellConstructorBackground))
        initializeLayoutOrigin(canvas)
        drawBackgroundGrid(canvas)
    }

    private fun initializeLayoutOrigin(canvas: Canvas?) {
        if (!isInitialized) {
            layout.origin = Point(canvas!!.width.toDouble() / 2.0,
                                  canvas.height.toDouble() / 2.0)
            isInitialized = true
        }
    }

    private fun drawBackgroundGrid(canvas: Canvas?) {
        for (hex in backgroundHexes) {
            canvas?.drawPath(getHexPath(hex), gridPaint)
        }
    }

    protected fun drawCoordinates(canvas: Canvas?) {
        for (hex in backgroundHexes) {
            drawCoordinatesOnHex(canvas, hex)
        }
    }

    protected fun drawCoordinatesOnHex(canvas: Canvas?, hex: Hex) {
        val hexCorners: ArrayList<Point> = Hex.poligonCorners(layout, hex)
        val center = Point(
                hexCorners.sumByDouble { it.x } / hexCorners.size.toDouble(),
                hexCorners.sumByDouble { it.y } / hexCorners.size.toDouble())
        val coef = 2.0
        val xOrigin = Point((hexCorners[5].x + center.x) / coef,
                            (hexCorners[5].y + center.y) / coef + coordinateTextVerticalOffset)
        val yOrigin = Point((hexCorners[3].x + center.x) / coef,
                            (hexCorners[3].y + center.y) / coef + coordinateTextVerticalOffset)
        val zOrigin = Point((hexCorners[1].x + center.x) / coef,
                            (hexCorners[1].y + center.y) / coef + coordinateTextVerticalOffset)
        canvas?.drawText(hex.q.toString() + "x", xOrigin.x.toFloat(), xOrigin.y.toFloat(), coordinateTextPaint)
        canvas?.drawText(hex.r.toString() + "y", yOrigin.x.toFloat(), yOrigin.y.toFloat(), coordinateTextPaint)
        canvas?.drawText(hex.s.toString() + "z", zOrigin.x.toFloat(), zOrigin.y.toFloat(), coordinateTextPaint)
    }

    protected fun getHexPath(hex: Hex): Path {
        val hexCorners: ArrayList<Point> = Hex.poligonCorners(layout, hex)
        val path = Path()
        path.moveTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        for (i in 1..(hexCorners.size - 1)) {
            path.lineTo(hexCorners[i].x.toFloat(), hexCorners[i].y.toFloat())
        }
        path.lineTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        return path
    }

    protected fun getLightHexPath(hex: Hex): Path {
        return getPartialHexPath(hex, 0, 3)
    }

    protected fun getDarkHexPath(hex: Hex): Path {
        return getPartialHexPath(hex, 3, 5, true)
    }

    protected fun getPartialHexPath(hex: Hex, startCorner: Int, endCorner: Int,
                                    endInFirstCorner: Boolean = false): Path {
        val hexCorners: ArrayList<Point> = Hex.poligonCorners(layout, hex)
        val path = Path()
        path.moveTo(hexCorners[startCorner].x.toFloat(), hexCorners[startCorner].y.toFloat())
        for (i in (startCorner + 1)..endCorner) {
            path.lineTo(hexCorners[i].x.toFloat(), hexCorners[i].y.toFloat())
        }
        if (endInFirstCorner) path.lineTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        return path
    }

}