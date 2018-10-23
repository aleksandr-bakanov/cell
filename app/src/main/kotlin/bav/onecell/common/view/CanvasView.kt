package bav.onecell.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import bav.onecell.R
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import bav.onecell.model.hexes.Layout
import bav.onecell.model.hexes.Orientation
import bav.onecell.model.hexes.Point
import kotlin.math.min

open class CanvasView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    companion object {
        private const val TAG = "CanvasView"
    }

    var backgroundFieldRadius: Int = 3
        set(value) {
            field = value
            backgroundHexes = hexMath.getNeighborsWithinRadius(hexMath.ZERO_HEX, value)
        }

    lateinit var hexMath: HexMath
    lateinit var drawUtils: DrawUtils

    protected var lastTouchX = 0f
    protected var lastTouchY = 0f
    protected var layout = Layout(Orientation.LAYOUT_POINTY, Point(Layout.DUMMY.size.x, Layout.DUMMY.size.y), Point())

    private val gridPaint = Paint()
    private val coordinateTextPaint = Paint()
    private val indexTextPaint = Paint()

    private var coordinateTextVerticalOffset = (layout.size.x / 10).toFloat()

    private lateinit var backgroundHexes: Set<Hex>

    init {
        gridPaint.style = Paint.Style.STROKE
        gridPaint.color = ContextCompat.getColor(context, R.color.cellEditorGrid)
        gridPaint.strokeWidth = 1.0f

        coordinateTextPaint.color = Color.BLACK
        coordinateTextPaint.textSize = 32f
        coordinateTextPaint.textAlign = Paint.Align.CENTER

        indexTextPaint.color = Color.RED
        indexTextPaint.textSize = 48f
        indexTextPaint.textAlign = Paint.Align.CENTER
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initializeLayout(w, h)
    }

    private fun initializeLayout(width: Int, height: Int) {
        val canvasSize = min(width, height)
        val hexesOnField = backgroundFieldRadius * 2 + 1
        val hexSize = (canvasSize / hexesOnField) / 2
        layout.size = Point(hexSize.toDouble(), hexSize.toDouble())
        layout.origin = Point(width.toDouble() / 2.0,
                              height.toDouble() / 2.0)
        coordinateTextVerticalOffset = (layout.size.x / 10).toFloat()
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

    private fun drawCellIndex(canvas: Canvas?, cell: Cell, index: Int = 0) {
        val origin = hexMath.hexToPixel(layout, cell.data.origin)
        canvas?.drawText(index.toString(), origin.x.toFloat(), origin.y.toFloat(), indexTextPaint)
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

    private fun getPartialHexPath(hex: Hex, startCorner: Int, endCorner: Int,
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

    open fun setLayoutSize(size: Double) {
        layout.size.x = size
        layout.size.y = size
    }
}
