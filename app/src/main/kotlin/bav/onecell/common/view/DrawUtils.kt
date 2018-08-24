package bav.onecell.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class DrawUtils(private val hexMath: HexMath, context: Context) {

    private val gridPaint = Paint()
    private val lifePaint = Paint()
    private val energyPaint = Paint()
    private val attackPaint = Paint()
    private val strokePaint = Paint()
    private val cellOutlinePaint = Paint()

    init {
        gridPaint.style = Paint.Style.STROKE
        gridPaint.color = ContextCompat.getColor(context, R.color.cellEditorGrid)
        gridPaint.strokeWidth = 1.0f

        lifePaint.style = Paint.Style.FILL
        lifePaint.color = ContextCompat.getColor(context, R.color.cellEditorLife)

        energyPaint.style = Paint.Style.FILL
        energyPaint.color = ContextCompat.getColor(context, R.color.cellEditorEnergy)

        attackPaint.style = Paint.Style.FILL
        attackPaint.color = ContextCompat.getColor(context, R.color.cellEditorAttack)

        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = ContextCompat.getColor(context, R.color.cellEditorStroke)
        strokePaint.strokeWidth = 1.0f

        cellOutlinePaint.style = Paint.Style.STROKE
        cellOutlinePaint.color = Color.BLACK
        cellOutlinePaint.strokeWidth = 5.0f
        cellOutlinePaint.strokeJoin = Paint.Join.ROUND
        cellOutlinePaint.strokeCap = Paint.Cap.ROUND
    }

    fun provideLayout(canvas: Canvas?, cellSize: Int): Layout {
        val layout = Layout(Orientation.LAYOUT_POINTY, Point(), Point())
        canvas?.let {
            val canvasSize = min(it.width, it.height)
            val hexSize = (canvasSize / max(cellSize, 1)) / 2
            layout.size = Point(hexSize.toDouble(), hexSize.toDouble())
            layout.origin = Point(it.width.toDouble() / 2.0,
                                  it.height.toDouble() / 2.0)
        }
        return layout
    }

    fun drawCell(canvas: Canvas?, cell: Cell?, lPaint: Paint = lifePaint, ePaint: Paint = energyPaint,
                 aPaint: Paint = attackPaint, layout: Layout = Layout.DUMMY) {
        cell?.let {
            var paint: Paint
            val originPoint = hexMath.hexToPixel(layout, it.data.origin)
            for ((_, hex) in it.data.hexes) {
                paint = when (hex.type) {
                    Hex.Type.LIFE -> lPaint
                    Hex.Type.ENERGY -> ePaint
                    Hex.Type.ATTACK -> aPaint
                    else -> gridPaint
                }
                val path: Path = getHexPath(layout, hexMath.add(hex, it.data.origin), originPoint, it.animationData.rotation)
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

    fun drawHexes(canvas: Canvas?, origin: Hex, hexes: Collection<Hex>?, paint: Paint, layout: Layout = Layout.DUMMY) {
        hexes?.forEach { hex ->
            val path: Path = getHexPath(layout, hexMath.add(hex, origin))
            path.fillType = Path.FillType.EVEN_ODD
            canvas?.drawPath(path, paint)
        }
    }

    private fun getHexPath(layout: Layout, hex: Hex, rotateAround: Point? = null, rotation: Float = 0f): Path {
        val hexCorners: ArrayList<Point> = hexMath.poligonCorners(layout, hex)
        rotateAround?.let { rotatePoints(hexCorners, it, rotation) }

        val path = Path()
        path.moveTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        for (i in 1..(hexCorners.size - 1)) {
            path.lineTo(hexCorners[i].x.toFloat(), hexCorners[i].y.toFloat())
        }
        path.lineTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())

        return path
    }

    private fun rotatePoints(points: List<Point>, origin: Point, angle: Float) {
        val s = sin(angle)
        val c = cos(angle)
        points.forEach { p ->
            // Translate point back to origin
            p.x -= origin.x
            p.y -= origin.y
            // Rotate point
            val newX = p.x * c - p.y * s
            val newY = p.x * s + p.y * c
            // Translate point back
            p.x = newX + origin.x
            p.y = newY + origin.y
        }
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
            canvas?.drawLine(it.first.x.toFloat(), it.first.y.toFloat(), it.second.x.toFloat(), it.second.y.toFloat(),
                             cellOutlinePaint)
        }
    }

    private fun getCellOutline(cell: Cell, layout: Layout): List<Pair<Point, Point>> {
        val lines = mutableListOf<Pair<Point, Point>>()
        cell.data.hexes.forEach {
            val hexCorners: ArrayList<Point> = hexMath.poligonCorners(layout, hexMath.add(it.value, cell.data.origin))
            rotatePoints(hexCorners, hexMath.hexToPixel(layout, cell.data.origin), cell.animationData.rotation)
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
