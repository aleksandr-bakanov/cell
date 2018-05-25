package bav.onecell.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import bav.onecell.R
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import bav.onecell.model.hexes.Layout
import bav.onecell.model.hexes.Orientation
import bav.onecell.model.hexes.Point
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class DrawUtils(private val context: Context, private val hexMath: HexMath) {

    private var layout = Layout(Orientation.LAYOUT_POINTY, Point(50.0, 50.0), Point())

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

    fun initializeLayout(canvas: Canvas?, cellSize: Int) {
        canvas?.let {
            val canvasSize = min(it.width, it.height)
            val hexSize = (canvasSize / cellSize) / 2
            layout.size = Point(hexSize.toDouble(), hexSize.toDouble())
            layout.origin = Point(it.width.toDouble() / 2.0,
                                  it.height.toDouble() / 2.0)
        }
    }

    fun drawCell(canvas: Canvas?, cell: Cell?, lPaint: Paint = lifePaint, ePaint: Paint = energyPaint,
                 aPaint: Paint = attackPaint, layout: Layout = Layout.DUMMY) {
        cell?.let {
            var paint: Paint
            for (hex in it.data.hexes) {
                paint = when (hex.value.type) {
                    Hex.Type.LIFE -> lPaint
                    Hex.Type.ENERGY -> ePaint
                    Hex.Type.ATTACK -> aPaint
                    else -> gridPaint
                }
                val path: Path = getHexPath(layout, hexMath.add(hex.value, it.data.origin))
                path.fillType = Path.FillType.EVEN_ODD
                canvas?.drawPath(path, paint)
                canvas?.drawPath(path, strokePaint)
            }
            // Draw origin marker
            drawOriginMarker(canvas, it, layout)
            // Draw outline
            drawCellOutline(canvas, it, layout)
        }
    }

    private fun getHexPath(layout: Layout, hex: Hex): Path {
        val hexCorners: ArrayList<Point> = hexMath.poligonCorners(layout, hex)
        val path = Path()
        path.moveTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        for (i in 1..(hexCorners.size - 1)) {
            path.lineTo(hexCorners[i].x.toFloat(), hexCorners[i].y.toFloat())
        }
        path.lineTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        return path
    }

    private fun drawOriginMarker(canvas: Canvas?, cell: Cell, layout: Layout) {
        // Origin point
        val o = hexMath.hexToPixel(layout, cell.data.origin)

        val angle = (-PI / 2) + (PI / 3) * cell.data.direction.ordinal
        // tail point
        var tp = Point(-layout.size.x * 2 / 3, 0.0)
        // head point
        var hp = Point(layout.size.x * 2 / 3, 0.0)
        // left point
        var lp = Point(layout.size.x / 3, -layout.size.x / 3)
        // right point
        var rp = Point(layout.size.x / 3, layout.size.x / 3)

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

    private fun drawCellOutline(canvas: Canvas?, cell: Cell, layout: Layout) {
        val outline = getCellOutline(cell, layout)
        outline.forEach {
            canvas?.drawLine(it.first.x.toFloat(), it.first.y.toFloat(), it.second.x.toFloat(), it.second.y.toFloat(), cellOutlinePaint)
        }
    }

    private fun getCellOutline(cell: Cell, layout: Layout): List<Pair<Point, Point>> {
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
}
