package bav.onecell.common.view

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import bav.onecell.R
import bav.onecell.common.Consts
import bav.onecell.model.battle.Bullet
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

class DrawUtils(private val hexMath: HexMath, private val context: Context) {

    companion object {
        private const val TAG = "BattleGraphics"
        private const val POWER_TEXT_SIZE = 64f
    }

    val gridPaint = Paint()
    val lifePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val energyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val attackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val deathRayHexPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val omniBulletHexPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val strokePaint = Paint()
    val groundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val cellOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val bulletOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val groupAffiliationFriendPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val groupAffiliationEnemyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val powerTextPaint = Paint()
    private val powerLifeTextPaint = Paint()
    private val powerEnergyTextPaint = Paint()
    private val powerAttackTextPaint = Paint()
    private val powerDeathRayTextPaint = Paint()
    private val powerOmniBulletTextPaint = Paint()
    val deathRayPaint = Paint(Paint.ANTI_ALIAS_FLAG)

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

        deathRayHexPaint.style = Paint.Style.FILL
        deathRayHexPaint.color = ContextCompat.getColor(context, R.color.cellEditorDeathRay)

        omniBulletHexPaint.style = Paint.Style.FILL
        omniBulletHexPaint.color = ContextCompat.getColor(context, R.color.cellEditorOmniBullet)

        deathRayPaint.color = ContextCompat.getColor(context, R.color.cellEditorDeathRay)
        deathRayPaint.strokeWidth = 10f
        deathRayPaint.strokeCap = Paint.Cap.ROUND

        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = ContextCompat.getColor(context, R.color.cellEditorStroke)
        strokePaint.strokeWidth = 2.0f

        cellOutlinePaint.style = Paint.Style.STROKE
        cellOutlinePaint.color = Color.BLACK
        cellOutlinePaint.strokeWidth = 5.0f
        cellOutlinePaint.strokeJoin = Paint.Join.ROUND
        cellOutlinePaint.strokeCap = Paint.Cap.ROUND

        bulletOutlinePaint.style = Paint.Style.STROKE
        bulletOutlinePaint.color = ContextCompat.getColor(context, R.color.bulletOutline)
        bulletOutlinePaint.strokeWidth = 5.0f
        bulletOutlinePaint.strokeJoin = Paint.Join.ROUND
        bulletOutlinePaint.strokeCap = Paint.Cap.ROUND

        groupAffiliationFriendPaint.style = Paint.Style.STROKE
        groupAffiliationFriendPaint.color = ContextCompat.getColor(context, R.color.battleFriendOutline)
        groupAffiliationFriendPaint.strokeWidth = 15.0f
        groupAffiliationFriendPaint.strokeJoin = Paint.Join.ROUND
        groupAffiliationFriendPaint.strokeCap = Paint.Cap.ROUND

        groupAffiliationEnemyPaint.style = Paint.Style.STROKE
        groupAffiliationEnemyPaint.color = ContextCompat.getColor(context, R.color.battleEnemyOutline)
        groupAffiliationEnemyPaint.strokeWidth = 15.0f
        groupAffiliationEnemyPaint.strokeJoin = Paint.Join.ROUND
        groupAffiliationEnemyPaint.strokeCap = Paint.Cap.ROUND

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

        powerDeathRayTextPaint.color = ContextCompat.getColor(context, R.color.battleViewPowerTextDeathRay)
        powerDeathRayTextPaint.typeface = Typeface.DEFAULT_BOLD
        powerDeathRayTextPaint.textSize = POWER_TEXT_SIZE
        powerDeathRayTextPaint.textAlign = Paint.Align.CENTER

        powerOmniBulletTextPaint.color = ContextCompat.getColor(context, R.color.battleViewPowerTextOmniBullet)
        powerOmniBulletTextPaint.typeface = Typeface.DEFAULT_BOLD
        powerOmniBulletTextPaint.textSize = POWER_TEXT_SIZE
        powerOmniBulletTextPaint.textAlign = Paint.Align.CENTER

        powerTextPaint.color = Color.BLACK
        powerTextPaint.typeface = Typeface.DEFAULT_BOLD
        powerTextPaint.textSize = POWER_TEXT_SIZE
        powerTextPaint.textAlign = Paint.Align.CENTER

        groundPaint.style = Paint.Style.FILL
        setGroundShader(R.drawable.battle_background_skilos)
    }

    fun setGroundShader(groundResourceId: Int) {
        val groundBitmap = BitmapFactory.decodeResource(context.resources, groundResourceId)
        val groundShader = BitmapShader(groundBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        groundPaint.shader = groundShader
    }

    class CellGraphicalPoints(
            var lifeHexes: MutableList<Path>? = null,
            var attackHexes: MutableList<Path>? = null,
            var energyHexes: MutableList<Path>? = null,
            var deathRayHexes: MutableList<Path>? = null,
            var omniBulletHexes: MutableList<Path>? = null,
            var outline: List<Point>? = null,
            var isFriendly: Boolean = false
    )

    fun getCellGraphicalRepresentation(cell: Cell): CellGraphicalPoints? {
        if (cell.data.hexes.isEmpty()) return null
        
        val graphicalPoints = CellGraphicalPoints()
        
        graphicalPoints.outline = getCellOutline(cell, Layout.UNIT)

        val originPoint = hexMath.hexToPixel(Layout.UNIT, cell.data.origin)
        val hexInGlobal = Hex()
        var pathsList: MutableList<Path>?
        for (hex in cell.data.hexes.values) {
            hexMath.add(hex, cell.data.origin, hexInGlobal)
            pathsList = when (hex.type) {
                Hex.Type.LIFE -> {
                    if (graphicalPoints.lifeHexes == null) graphicalPoints.lifeHexes = mutableListOf()
                    graphicalPoints.lifeHexes
                }
                Hex.Type.ENERGY -> {
                    if (graphicalPoints.energyHexes == null) graphicalPoints.energyHexes = mutableListOf()
                    graphicalPoints.energyHexes
                }
                Hex.Type.ATTACK -> {
                    if (graphicalPoints.attackHexes == null) graphicalPoints.attackHexes = mutableListOf()
                    graphicalPoints.attackHexes
                }
                Hex.Type.DEATH_RAY -> {
                    if (graphicalPoints.deathRayHexes == null) graphicalPoints.deathRayHexes = mutableListOf()
                    graphicalPoints.deathRayHexes
                }
                Hex.Type.OMNI_BULLET -> {
                    if (graphicalPoints.omniBulletHexes == null) graphicalPoints.omniBulletHexes = mutableListOf()
                    graphicalPoints.omniBulletHexes
                }
                else -> null
            }

            var fadeScale = 1.0f
            cell.animationData.hexHashesToRemove?.let { hexHashesToRemove ->
                if (hexHashesToRemove.contains(hex.mapKey)) {
                    fadeScale = 1.0f - cell.animationData.fadeFraction
                }
            }

            val points = getHexPath(Layout.UNIT, hexInGlobal, originPoint,
                                    cell.animationData.rotation, cell.animationData.moveDirection,
                                    cell.animationData.movingFraction, scale = fadeScale)
            pathsList?.add(points)
        }
        graphicalPoints.isFriendly = cell.data.groupId == Consts.MAIN_CHARACTERS_GROUP_ID
        return graphicalPoints
    }

    private val transformedHexPath: Path = Path()
    fun drawCellGraphicalRepresentation(canvas: Canvas?, graphics: CellGraphicalPoints, layout: Layout,
                                        layoutMatrix: Matrix,
                                        lPaint: Paint = lifePaint, ePaint: Paint = energyPaint,
                                        aPaint: Paint = attackPaint, dPaint: Paint = deathRayHexPaint,
                                        oPaint: Paint = omniBulletHexPaint,
                                        isCorpse: Boolean) {
        // Group affiliation outline
        if (!isCorpse) {
            graphics.outline?.let { points ->
                for (i in 0 until points.size step 2) {
                    canvas?.drawLine((points[i].x * layout.size.x + layout.origin.x).toFloat(),
                                     (points[i].y * layout.size.y + layout.origin.y).toFloat(),
                                     (points[i + 1].x * layout.size.x + layout.origin.x).toFloat(),
                                     (points[i + 1].y * layout.size.y + layout.origin.y).toFloat(),
                                     if (graphics.isFriendly)
                                         groupAffiliationFriendPaint else groupAffiliationEnemyPaint)
                }
            }
        }

        // Hexes
        graphics.lifeHexes?.let { paths ->
            for (path in paths) {
                path.transform(layoutMatrix, transformedHexPath)
                canvas?.drawPath(transformedHexPath, lPaint)
            }
        }
        graphics.attackHexes?.let { paths ->
            for (path in paths) {
                path.transform(layoutMatrix, transformedHexPath)
                canvas?.drawPath(transformedHexPath, aPaint)
            }
        }
        graphics.energyHexes?.let { paths ->
            for (path in paths) {
                path.transform(layoutMatrix, transformedHexPath)
                canvas?.drawPath(transformedHexPath, ePaint)
            }
        }
        graphics.deathRayHexes?.let { paths ->
            for (path in paths) {
                path.transform(layoutMatrix, transformedHexPath)
                canvas?.drawPath(transformedHexPath, dPaint)
            }
        }
        graphics.omniBulletHexes?.let { paths ->
            for (path in paths) {
                path.transform(layoutMatrix, transformedHexPath)
                canvas?.drawPath(transformedHexPath, oPaint)
            }
        }

        // Outline
        graphics.outline?.let { points ->
            for (i in 0 until points.size step 2) {
                canvas?.drawLine((points[i].x * layout.size.x + layout.origin.x).toFloat(),
                                 (points[i].y * layout.size.y + layout.origin.y).toFloat(),
                                 (points[i + 1].x * layout.size.x + layout.origin.x).toFloat(),
                                 (points[i + 1].y * layout.size.y + layout.origin.y).toFloat(),
                                 cellOutlinePaint)
            }
        }
    }

    fun drawCell(canvas: Canvas?, cell: Cell?, lPaint: Paint = lifePaint, ePaint: Paint = energyPaint,
                 aPaint: Paint = attackPaint, dPaint: Paint = deathRayHexPaint, oPaint: Paint = omniBulletHexPaint,
                 layout: Layout = Layout.DUMMY, drawAffiliation: Boolean = false, gPaint: Paint = groundPaint) {
        cell?.let {
            val outline = getCellOutline(cell, layout)
            // Draw depiction of group
            if (drawAffiliation) {
                val groupAffiliationPaint = if (it.data.groupId == Consts.MAIN_CHARACTERS_GROUP_ID) groupAffiliationFriendPaint else groupAffiliationEnemyPaint
                drawCellOutline(canvas, it, layout, groupAffiliationPaint, outline)
            }

            var paint: Paint
            val originPoint = hexMath.hexToPixel(layout, it.data.origin)
            val hexInGlobal = Hex()
            for (hex in it.data.hexes.values) {
                hexMath.add(hex, it.data.origin, hexInGlobal)
                paint = when (hex.type) {
                    Hex.Type.LIFE -> lPaint
                    Hex.Type.ENERGY -> ePaint
                    Hex.Type.ATTACK -> aPaint
                    Hex.Type.DEATH_RAY -> dPaint
                    Hex.Type.OMNI_BULLET -> oPaint
                    else -> gridPaint
                }

                var fadeScale = 1.0f
                it.animationData.hexHashesToRemove?.let { hexHashesToRemove ->
                    if (hexHashesToRemove.contains(hex.mapKey)) {
                        fadeScale = 1.0f - it.animationData.fadeFraction
                    }
                }

                val path: Path = getHexPath(layout, hexInGlobal, originPoint,
                                            it.animationData.rotation, it.animationData.moveDirection,
                                            it.animationData.movingFraction, scale = fadeScale)

                if (fadeScale != 1.0f) {
                    val groundPath: Path = getHexPath(layout, hexInGlobal, originPoint,
                                                it.animationData.rotation, it.animationData.moveDirection,
                                                it.animationData.movingFraction)
                    groundPath.fillType = Path.FillType.EVEN_ODD
                    canvas?.drawPath(groundPath, gPaint)
                }

                path.fillType = Path.FillType.EVEN_ODD
                canvas?.drawPath(path, paint)
            }
            // Draw origin marker
            drawOriginMarker(canvas, it, layout)
            // Draw outline
            drawCellOutline(canvas, it, layout, cellOutlinePaint, outline)
        }
    }

    fun drawHexes(canvas: Canvas?, origin: Hex, hexes: Collection<Hex>?, paint: Paint, layout: Layout = Layout.DUMMY,
                  scale: Float = 1.0f) {
        val hexInGlobal = Hex()
        hexes?.forEach { hex ->
            hexMath.add(hex, origin, hexInGlobal)
            val path: Path = getHexPath(layout, hexInGlobal, scale = scale)
            path.fillType = Path.FillType.EVEN_ODD
            canvas?.drawPath(path, paint)
        }
    }

    fun getBulletPath(bullet: Bullet, layout: Layout = Layout.DUMMY): Path {
        return getHexPath(layout, bullet.origin, movingDirection = bullet.direction,
                           movingFraction = bullet.movingFraction, scale = 0.5f)
    }

    private fun getHexPath(layout: Layout, hex: Hex, rotateAround: Point? = null, rotation: Float = 0f,
                           movingDirection: Int = 0, movingFraction: Float = 0f, scale: Float = 1f): Path {
        val hexCorners = hexMath.polygonCorners(layout, hex, scale)

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

    private fun offsetPoints(points: List<Point>, direction: Int, fraction: Float, layout: Layout, point: Point? = null) {
        val offsetPoint = point ?: hexMath.hexToPixel(layout, hexMath.getHexByDirection(direction))
        points.forEach { p ->
            p.x += (offsetPoint.x - layout.origin.x) * fraction
            p.y += (offsetPoint.y - layout.origin.y) * fraction
        }
    }

    fun offsetPoint(point: Point, direction: Int, fraction: Float, layout: Layout) {
        val offsetPoint = hexMath.hexToPixel(layout, hexMath.getHexByDirection(direction))
        point.x += (offsetPoint.x - layout.origin.x) * fraction
        point.y += (offsetPoint.y - layout.origin.y) * fraction
    }

    fun rotatePoint(point: Point, origin: Point, angle: Float) {
        val s = sin(angle)
        val c = cos(angle)
        // Translate point back to origin
        point.x -= origin.x
        point.y -= origin.y
        // Rotate point
        val newX = point.x * c - point.y * s
        val newY = point.x * s + point.y * c
        // Translate point back
        point.x = newX + origin.x
        point.y = newY + origin.y
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

    private fun drawCellOutline(canvas: Canvas?, cell: Cell, layout: Layout, paint: Paint = cellOutlinePaint,
                                lines: List<Point>? = null) {
        val outline = lines ?: getCellOutline(cell, layout)
        for (i in 0 until outline.size step 2) {
            canvas?.drawLine(outline[i].x.toFloat(), outline[i].y.toFloat(),
                             outline[i + 1].x.toFloat(), outline[i + 1].y.toFloat(),
                             paint)
        }
    }

    private fun getCellOutline(cell: Cell, layout: Layout): List<Point> {
        val lines = mutableListOf<Point>()
        val neighbor = Hex()
        val offsetPoint = hexMath.hexToPixel(layout, hexMath.getHexByDirection(cell.animationData.moveDirection))
        val cellOrigin = hexMath.hexToPixel(layout, cell.data.origin)
        val hexInGlobal = Hex()
        val hexCorners: MutableList<Point> = mutableListOf()
        for (i in 0..5) hexCorners.add(Point())

        cell.data.hexes.values.forEach { hex ->
            hexMath.add(hex, cell.data.origin, hexInGlobal)
            hexMath.polygonCorners(layout, hexInGlobal, hexCorners)

            // Rotate and offset
            rotatePoints(hexCorners, cellOrigin, cell.animationData.rotation)
            if (cell.animationData.movingFraction > 0f)
                offsetPoints(hexCorners, cell.animationData.moveDirection, cell.animationData.movingFraction, layout, offsetPoint)

            for (direction in 0..5) {
                hexMath.getHexNeighbor(hex, direction, neighbor)
                if (!cell.data.hexes.values.contains(neighbor)) {
                    when (direction) {
                        0 -> { lines.add(hexCorners[4].copy()); lines.add(hexCorners[5].copy()) }
                        1 -> { lines.add(hexCorners[5].copy()); lines.add(hexCorners[0].copy()) }
                        2 -> { lines.add(hexCorners[0].copy()); lines.add(hexCorners[1].copy()) }
                        3 -> { lines.add(hexCorners[1].copy()); lines.add(hexCorners[2].copy()) }
                        4 -> { lines.add(hexCorners[2].copy()); lines.add(hexCorners[3].copy()) }
                        5 -> { lines.add(hexCorners[3].copy()); lines.add(hexCorners[4].copy()) }
                        else -> Unit
                    }
                }
            }
        }
        return lines
    }

    fun drawCellPower(canvas: Canvas?, cell: Cell, layout: Layout) {
        val hexInGlobal = Hex()
        cell.data.hexes.values.forEach { hex ->
            hexMath.add(cell.data.origin, hex, hexInGlobal)
            val paint = when (hex.type) {
                Hex.Type.LIFE -> powerLifeTextPaint
                Hex.Type.ENERGY -> powerEnergyTextPaint
                Hex.Type.ATTACK -> powerAttackTextPaint
                Hex.Type.DEATH_RAY -> powerDeathRayTextPaint
                Hex.Type.OMNI_BULLET -> powerOmniBulletTextPaint
                else -> powerTextPaint
            }
            paint.textSize = layout.size.x.toFloat()
            drawHexPower(canvas, layout, cell, hexInGlobal, hex.power, paint)
        }
    }

    private fun drawHexPower(canvas: Canvas?, layout: Layout, cell: Cell, hex: Hex, power: Int, paint: Paint) {
        val origin = hexMath.hexToPixel(layout, hex)
        val listOfOrigin = listOf(origin)
        rotatePoints(listOfOrigin, hexMath.hexToPixel(layout, cell.data.origin), cell.animationData.rotation)
        offsetPoints(listOfOrigin, cell.animationData.moveDirection, cell.animationData.movingFraction, layout)
        canvas?.drawText(power.toString(), origin.x.toFloat(), origin.y.toFloat() + (layout.size.x / 3).toFloat(),
                         paint)
    }
}
