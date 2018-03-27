package bav.onecell.constructor

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
import bav.onecell.model.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Layout
import bav.onecell.model.hexes.Orientation
import bav.onecell.model.hexes.Point
import java.lang.Math.max
import java.lang.Math.min

class ConstructorCanvasView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    companion object {
        private val TAG = "ConstructorCanvasView"
    }

    var backgroundFieldRadius: Int = 1
        set(value) {
            backgroundHexes.clear()
            for (q in -value..value) {
                val r1 = max(-value, -q - value)
                val r2 = min(value, -q + value)
                for (r in r1..r2) {
                    backgroundHexes.add(Hex(q, r, -q - r))
                }
            }
            invalidate()
        }
    var cell: Cell? = null
    lateinit var presenter: Constructor.Presenter
    var selectedCellType: Hex.Type = Hex.Type.LIFE

    private val layoutHexSize = Point(100.0, 100.0)
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val layout = Layout(
            Orientation.LAYOUT_POINTY, layoutHexSize, Point(100.0, 100.0))

    private val gridPaint: Paint = Paint()
    private val lifePaint: Paint = Paint()
    private val energyPaint: Paint = Paint()
    private val attackPaint: Paint = Paint()
    private val strokePaint: Paint = Paint()
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

        setOnTouchListener(
                { _: View?, event: MotionEvent? ->
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
                    } else if (event?.action == MotionEvent.ACTION_DOWN) {
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

        canvas?.drawColor(ContextCompat.getColor(context, R.color.cellConstructorBackground))

        initializeLayoutOrigin(canvas)
        drawBackgroundGrid(canvas)
        drawCell(canvas)
        drawCoordinates(canvas)
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

    private fun drawCoordinates(canvas: Canvas?) {
        for (hex in backgroundHexes) {
            drawCoordinatesOnHex(canvas, hex)
        }
    }

    private fun drawCoordinatesOnHex(canvas: Canvas?, hex: Hex) {
        val hexCorners: ArrayList<Point> = Hex.poligonCorners(layout, hex)
        val center = Point(
                hexCorners.sumByDouble { it.x } / hexCorners.size.toDouble(),
                hexCorners.sumByDouble { it.y } / hexCorners.size.toDouble())
        val coef = 2.0
        val xOrigin = Point((hexCorners[5].x + center.x) / coef, (hexCorners[5].y + center.y) / coef + coordinateTextVerticalOffset)
        val yOrigin = Point((hexCorners[3].x + center.x) / coef, (hexCorners[3].y + center.y) / coef + coordinateTextVerticalOffset)
        val zOrigin = Point((hexCorners[1].x + center.x) / coef, (hexCorners[1].y + center.y) / coef + coordinateTextVerticalOffset)
        canvas?.drawText(hex.q.toString() + "x", xOrigin.x.toFloat(), xOrigin.y.toFloat(), coordinateTextPaint)
        canvas?.drawText(hex.r.toString() + "y", yOrigin.x.toFloat(), yOrigin.y.toFloat(), coordinateTextPaint)
        canvas?.drawText(hex.s.toString() + "z", zOrigin.x.toFloat(), zOrigin.y.toFloat(), coordinateTextPaint)
    }

    private fun getHexPath(hex: Hex): Path {
        val hexCorners: ArrayList<Point> = Hex.poligonCorners(layout, hex)
        val path = Path()
        path.moveTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        for (i in 1..(hexCorners.size - 1)) {
            path.lineTo(hexCorners[i].x.toFloat(), hexCorners[i].y.toFloat())
        }
        path.lineTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        return path
    }

    private fun getLightHexPath(hex: Hex): Path {
        return getPartialHexPath(hex, 0, 3)
    }

    private fun getDarkHexPath(hex: Hex): Path {
        return getPartialHexPath(hex, 3, 5, true)
    }

    private fun getPartialHexPath(hex: Hex, startCorner: Int, endCorner: Int, endInFirstCorner: Boolean = false): Path {
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
