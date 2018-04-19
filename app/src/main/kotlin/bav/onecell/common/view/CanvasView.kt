package bav.onecell.common.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import bav.onecell.R
import bav.onecell.model.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import bav.onecell.model.hexes.Layout
import bav.onecell.model.hexes.Orientation
import bav.onecell.model.hexes.Point
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

open class CanvasView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    companion object {
        private const val TAG = "CanvasView"
    }

    var backgroundFieldRadius: Int = 1
        set(value) {
            field = value
            backgroundHexes = hexMath.getNeighborsWithinRadius(hexMath.ZERO_HEX, value)
            isInitialized = false
        }

    lateinit var hexMath: HexMath

    private var layoutHexSize = Point(50.0, 50.0)
        set(value) {
            field = value
            layout = Layout(Orientation.LAYOUT_POINTY, layoutHexSize, Point())
            coordinateTextVerticalOffset = (layoutHexSize.x / 10).toFloat()
        }

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    protected var layout = Layout(Orientation.LAYOUT_POINTY, layoutHexSize, Point())

    private val gridPaint = Paint()
    private val lifePaint = Paint()
    private val energyPaint = Paint()
    private val attackPaint = Paint()
    private val strokePaint = Paint()
    private val cellOutlinePaint = Paint()
    private val darkStrokePaint = Paint()
    private val lightStrokePaint = Paint()
    private val coordinateTextPaint = Paint()
    private val indexTextPaint = Paint()
    private val powerTextPaint = Paint()
    private var coordinateTextVerticalOffset = (layoutHexSize.x / 10).toFloat()

    private var isInitialized = false
    private lateinit var backgroundHexes: Set<Hex>

    init {
        gridPaint.style = Paint.Style.STROKE
        gridPaint.color = ContextCompat.getColor(context, R.color.cellConstructorGrid)
        gridPaint.strokeWidth = 1.0f

        lifePaint.style = Paint.Style.FILL
        lifePaint.color = ContextCompat.getColor(context, R.color.cellConstructorLife)

        energyPaint.style = Paint.Style.FILL
        energyPaint.color = ContextCompat.getColor(context, R.color.cellConstructorEnergy)

        attackPaint.style = Paint.Style.FILL
        attackPaint.color = ContextCompat.getColor(context, R.color.cellConstructorAttack)

        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = ContextCompat.getColor(context, R.color.cellConstructorStroke)
        strokePaint.strokeWidth = 1.0f

        darkStrokePaint.style = Paint.Style.STROKE
        darkStrokePaint.color = Color.BLACK
        darkStrokePaint.strokeWidth = 1.0f

        cellOutlinePaint.style = Paint.Style.STROKE
        cellOutlinePaint.color = Color.BLACK
        cellOutlinePaint.strokeWidth = 3.0f
        cellOutlinePaint.strokeJoin = Paint.Join.ROUND
        cellOutlinePaint.strokeCap = Paint.Cap.ROUND

        lightStrokePaint.style = Paint.Style.STROKE
        lightStrokePaint.color = Color.WHITE
        lightStrokePaint.strokeWidth = 1.0f

        coordinateTextPaint.color = Color.BLACK
        coordinateTextPaint.textSize = 32f
        coordinateTextPaint.textAlign = Paint.Align.CENTER

        indexTextPaint.color = Color.RED
        indexTextPaint.textSize = 48f
        indexTextPaint.textAlign = Paint.Align.CENTER

        powerTextPaint.color = Color.BLACK
        powerTextPaint.typeface = Typeface.DEFAULT_BOLD
        powerTextPaint.textSize = 72f
        powerTextPaint.textAlign = Paint.Align.CENTER
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
        if (!isInitialized) {
            initializeLayout(canvas)
            isInitialized = true
        }
        drawBackgroundGrid(canvas)
    }

    private fun initializeLayout(canvas: Canvas?) {
        canvas?.let {
            val canvasSize = min(it.width, it.height)
            val hexesOnField = backgroundFieldRadius * 2 + 1
            val hexSize = (canvasSize / hexesOnField) / 2
            layoutHexSize = Point(hexSize.toDouble(), hexSize.toDouble())
            layout.origin = Point(it.width.toDouble() / 2.0,
                                  it.height.toDouble() / 2.0)
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

    protected fun drawCornersIndexes(canvas: Canvas?) {
        for (hex in backgroundHexes) {
            drawHexCornerIndexes(canvas, hex)
        }
    }

    protected fun drawCell(canvas: Canvas?,
                           cell: Cell?,
                           lPaint: Paint = lifePaint,
                           ePaint: Paint = energyPaint,
                           aPaint: Paint = attackPaint) {
        cell?.let {
            var paint: Paint
            for (hex in it.data.hexes) {
                paint = when (hex.value.type) {
                    Hex.Type.LIFE -> lPaint
                    Hex.Type.ENERGY -> ePaint
                    Hex.Type.ATTACK -> aPaint
                    else -> gridPaint
                }
                val path: Path = getHexPath(hexMath.add(hex.value, it.data.origin))
                path.fillType = Path.FillType.EVEN_ODD
                canvas?.drawPath(path, paint)
                canvas?.drawPath(path, strokePaint)
            }
            // Draw origin marker
            drawOriginMarker(canvas, cell)
            // Draw outline
            drawCellOutline(canvas, cell)
            // Draw powers
//            drawCellPower(canvas, cell)
        }
    }

    private fun drawCellIndex(canvas: Canvas?, cell: Cell, index: Int = 0) {
        val origin = hexMath.hexToPixel(layout, cell.data.origin)
        canvas?.drawText(index.toString(), origin.x.toFloat(), origin.y.toFloat(), indexTextPaint)
    }

    private fun drawCellPower(canvas: Canvas?, cell: Cell) {
        cell.data.hexes.forEach {
            drawHexPower(canvas, hexMath.add(cell.data.origin, it.value), it.value.power)
        }
    }

    private fun drawHexPower(canvas: Canvas?, hex: Hex, power: Int) {
        val origin = hexMath.hexToPixel(layout, hex)
        canvas?.drawText(power.toString(), origin.x.toFloat(), origin.y.toFloat() + (layoutHexSize.x / 2).toFloat(), powerTextPaint)
    }

    private fun drawOriginMarker(canvas: Canvas?, cell: Cell) {
        // Origin point
        val o = hexMath.hexToPixel(layout, cell.data.origin)

        val angle = (-PI / 2) + (PI / 3) * cell.data.direction.ordinal
        // tail point
        var tp = Point(-layoutHexSize.x * 2 / 3, 0.0)
        // head point
        var hp = Point(layoutHexSize.x * 2 / 3, 0.0)
        // left point
        var lp = Point(layoutHexSize.x / 3, -layoutHexSize.x / 3)
        // right point
        var rp = Point(layoutHexSize.x / 3, layoutHexSize.x / 3)

        // Rotate arrow points
        tp = Point(tp.x * cos(angle) - tp.y * sin(angle), tp.x * sin(angle) + tp.y * cos(angle))
        hp = Point(hp.x * cos(angle) - hp.y * sin(angle), hp.x * sin(angle) + hp.y * cos(angle))
        lp = Point(lp.x * cos(angle) - lp.y * sin(angle), lp.x * sin(angle) + lp.y * cos(angle))
        rp = Point(rp.x * cos(angle) - rp.y * sin(angle), rp.x * sin(angle) + rp.y * cos(angle))

        // Draw lines
        canvas?.drawLine(tp.x.toFloat() + o.x.toFloat(), tp.y.toFloat() + o.y.toFloat(),
                         hp.x.toFloat() + o.x.toFloat(), hp.y.toFloat() + o.y.toFloat(), strokePaint)
        canvas?.drawLine(lp.x.toFloat() + o.x.toFloat(), lp.y.toFloat() + o.y.toFloat(),
                         hp.x.toFloat() + o.x.toFloat(), hp.y.toFloat() + o.y.toFloat(), strokePaint)
        canvas?.drawLine(rp.x.toFloat() + o.x.toFloat(), rp.y.toFloat() + o.y.toFloat(),
                         hp.x.toFloat() + o.x.toFloat(), hp.y.toFloat() + o.y.toFloat(), strokePaint)
    }

    private fun drawCellOutline(canvas: Canvas?, cell: Cell) {
        val outline = getCellOutline(cell)
        outline.forEach {
            canvas?.drawLine(it.first.x.toFloat(), it.first.y.toFloat(), it.second.x.toFloat(), it.second.y.toFloat(), cellOutlinePaint)
        }
    }

    private fun getCellOutline(cell: Cell): List<Pair<Point, Point>> {
        val lines = mutableListOf<Pair<Point, Point>>()
        cell.data.hexes.forEach {
            val hexCorners: ArrayList<Point> = hexMath.poligonCorners(layout, hexMath.add(it.value, cell.data.origin))
            for (direction in 0..5) {
                val neighbor = hexMath.getHexNeighbor(it.value, direction)
                if (!cell.data.hexes.values.contains(neighbor)) {
                    lines.add(getHexSideByNeighborDirection(hexCorners, direction))
                }
            }
        }
        return lines
    }

    private fun getHexSideByNeighborDirection(corners: List<Point>, direction: Int): Pair<Point, Point> {
        return when (direction) {
            0 -> Pair(corners[4], corners[5])
            1 -> Pair(corners[5], corners[0])
            2 -> Pair(corners[0], corners[1])
            3 -> Pair(corners[1], corners[2])
            4 -> Pair(corners[2], corners[3])
            5 -> Pair(corners[3], corners[4])
            else -> Pair(corners[0], corners[0])
        }
    }

    private fun drawHexCornerIndexes(canvas: Canvas?, hex: Hex) {
        val hexCorners: ArrayList<Point> = hexMath.poligonCorners(layout, hex)
        for (i in 0..(hexCorners.size - 1)) {
            canvas?.drawText(i.toString(), hexCorners[i].x.toFloat(), hexCorners[i].y.toFloat(), coordinateTextPaint)
        }
    }

    private fun drawCoordinatesOnHex(canvas: Canvas?, hex: Hex) {
        val hexCorners: ArrayList<Point> = hexMath.poligonCorners(layout, hex)
        val center = Point(
                hexCorners.sumByDouble { it.x } / hexCorners.size.toDouble(),
                hexCorners.sumByDouble { it.y } / hexCorners.size.toDouble())
        val coef = 2.0
        val qOrigin = Point((hexCorners[5].x + center.x) / coef,
                            (hexCorners[5].y + center.y) / coef + coordinateTextVerticalOffset)
        val rOrigin = Point((hexCorners[1].x + center.x) / coef,
                            (hexCorners[1].y + center.y) / coef + coordinateTextVerticalOffset)
        val sOrigin = Point((hexCorners[3].x + center.x) / coef,
                            (hexCorners[3].y + center.y) / coef + coordinateTextVerticalOffset)
        canvas?.drawText(hex.q.toString() + "q", qOrigin.x.toFloat(), qOrigin.y.toFloat(), coordinateTextPaint)
        canvas?.drawText(hex.r.toString() + "r", rOrigin.x.toFloat(), rOrigin.y.toFloat(), coordinateTextPaint)
        canvas?.drawText(hex.s.toString() + "s", sOrigin.x.toFloat(), sOrigin.y.toFloat(), coordinateTextPaint)
    }

    protected fun getHexPath(hex: Hex): Path {
        val hexCorners: ArrayList<Point> = hexMath.poligonCorners(layout, hex)
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
        val hexCorners: ArrayList<Point> = hexMath.poligonCorners(layout, hex)
        val path = Path()
        path.moveTo(hexCorners[startCorner].x.toFloat(), hexCorners[startCorner].y.toFloat())
        for (i in (startCorner + 1)..endCorner) {
            path.lineTo(hexCorners[i].x.toFloat(), hexCorners[i].y.toFloat())
        }
        if (endInFirstCorner) path.lineTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        return path
    }

}