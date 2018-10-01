package bav.onecell.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.content.ContextCompat
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

    companion object {
        private const val POWER_TEXT_SIZE = 64f
    }

    private val gridPaint = Paint()
    private val lifePaint = Paint()
    private val energyPaint = Paint()
    private val attackPaint = Paint()
    private val strokePaint = Paint()
    private val cellOutlinePaint = Paint()
    private val powerTextPaint = Paint()
    private val powerLifeTextPaint = Paint()
    private val powerEnergyTextPaint = Paint()
    private val powerAttackTextPaint = Paint()

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

        powerLifeTextPaint.color = ContextCompat.getColor(context, R.color.battleViewPowerTextLife)
        powerLifeTextPaint.typeface = Typeface.DEFAULT_BOLD
        powerLifeTextPaint.textSize = POWER_TEXT_SIZE
        powerLifeTextPaint.textAlign = Paint.Align.CENTER

        powerEnergyTextPaint.color = ContextCompat.getColor(context, R.color.battleViewPowerTextEnergy)
        powerEnergyTextPaint.typeface = Typeface.DEFAULT_BOLD
        powerEnergyTextPaint.textSize = POWER_TEXT_SIZE
        powerEnergyTextPaint.textAlign = Paint.Align.CENTER

        powerAttackTextPaint.color = ContextCompat.getColor(context, R.color.battleViewPowerTextAttack)
        powerAttackTextPaint.typeface = Typeface.DEFAULT_BOLD
        powerAttackTextPaint.textSize = POWER_TEXT_SIZE
        powerAttackTextPaint.textAlign = Paint.Align.CENTER

        powerTextPaint.color = Color.BLACK
        powerTextPaint.typeface = Typeface.DEFAULT_BOLD
        powerTextPaint.textSize = POWER_TEXT_SIZE
        powerTextPaint.textAlign = Paint.Align.CENTER
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
                val path: Path = getHexPath(layout, hexMath.add(hex, it.data.origin), originPoint,
                                            it.animationData.rotation, it.animationData.moveDirection,
                                            it.animationData.movingFraction)

                val oldPaintAlpha = paint.alpha
                it.animationData.hexHashesToRemove?.let { hexHashesToRemove ->
                    if (hexHashesToRemove.contains(hex.hashCode())) {
                        paint.alpha = 255 - (it.animationData.fadeFraction * 255f).toInt()
                    }
                }

                path.fillType = Path.FillType.EVEN_ODD
                canvas?.drawPath(path, paint)
                canvas?.drawPath(path, strokePaint)
                paint.alpha = oldPaintAlpha
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

    private fun getHexPath(layout: Layout, hex: Hex, rotateAround: Point? = null, rotation: Float = 0f,
                           movingDirection: Int = 0, movingFraction: Float = 0f): Path {
        val hexCorners: ArrayList<Point> = hexMath.poligonCorners(layout, hex)

        // Rotation and moving offset
        rotateAround?.let { rotatePoints(hexCorners, it, rotation) }
        if (movingFraction > 0f) offsetPoints(hexCorners, movingDirection, movingFraction, layout)

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

    private fun offsetPoints(points: List<Point>, direction: Int, fraction: Float, layout: Layout) {
        val offsetPoint = hexMath.hexToPixel(layout, hexMath.getHexByDirection(direction))
        points.forEach { p ->
            p.x += (offsetPoint.x - layout.origin.x) * fraction
            p.y += (offsetPoint.y - layout.origin.y) * fraction
        }
    }

    private fun drawOriginMarker(canvas: Canvas?, cell: Cell, layout: Layout) {
        // Origin point
        val o = hexMath.hexToPixel(layout, cell.data.origin)
        offsetPoints(listOf(o), cell.animationData.moveDirection, cell.animationData.movingFraction, layout)

        val angle = (-PI / 2) + (PI / 3) * cell.data.direction.ordinal
        // tail point
        var tp = Point(-layout.size.x * 2 / 3, 0.0)
        // head point
        var hp = Point(layout.size.x * 2 / 3, 0.0)
        // left point
        var lp = Point(layout.size.x / 3, -layout.size.x / 3)
        // right point
        var rp = Point(layout.size.x / 3, layout.size.x / 3)
        
        val s = sin(angle)
        val c = cos(angle)

        // Rotate arrow points
        tp = Point(tp.x * c - tp.y * s, tp.x * s + tp.y * c)
        hp = Point(hp.x * c - hp.y * s, hp.x * s + hp.y * c)
        lp = Point(lp.x * c - lp.y * s, lp.x * s + lp.y * c)
        rp = Point(rp.x * c - rp.y * s, rp.x * s + rp.y * c)
        
        val oxf = o.x.toFloat()
        val oyf = o.y.toFloat()

        // Draw lines
        canvas?.drawLine(tp.x.toFloat() + oxf, tp.y.toFloat() + oyf,
                         hp.x.toFloat() + oxf, hp.y.toFloat() + oyf, strokePaint)
        canvas?.drawLine(lp.x.toFloat() + oxf, lp.y.toFloat() + oyf,
                         hp.x.toFloat() + oxf, hp.y.toFloat() + oyf, strokePaint)
        canvas?.drawLine(rp.x.toFloat() + oxf, rp.y.toFloat() + oyf,
                         hp.x.toFloat() + oxf, hp.y.toFloat() + oyf, strokePaint)
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

            // Rotate and offset
            rotatePoints(hexCorners, hexMath.hexToPixel(layout, cell.data.origin), cell.animationData.rotation)
            if (cell.animationData.movingFraction > 0f)
                offsetPoints(hexCorners, cell.animationData.moveDirection, cell.animationData.movingFraction, layout)

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

    fun drawCellPower(canvas: Canvas?, cell: Cell, layout: Layout) {
        cell.data.hexes.forEach {
            val paint = when (it.value.type) {
                Hex.Type.LIFE -> powerLifeTextPaint
                Hex.Type.ENERGY -> powerEnergyTextPaint
                Hex.Type.ATTACK -> powerAttackTextPaint
                else -> powerTextPaint
            }
            drawHexPower(canvas, layout, cell, hexMath.add(cell.data.origin, it.value), it.value.power, paint)
        }
    }

    private fun drawHexPower(canvas: Canvas?, layout: Layout, cell: Cell, hex: Hex, power: Int, paint: Paint) {
        val origin = hexMath.hexToPixel(layout, hex)
        val listOfOrigin = listOf(origin)
        rotatePoints(listOfOrigin, hexMath.hexToPixel(layout, cell.data.origin), cell.animationData.rotation)
        offsetPoints(listOfOrigin, cell.animationData.moveDirection, cell.animationData.movingFraction, layout)
        canvas?.drawText(power.toString(), origin.x.toFloat(), origin.y.toFloat() + (layout.size.x / 2).toFloat(),
                         paint)
    }
}
