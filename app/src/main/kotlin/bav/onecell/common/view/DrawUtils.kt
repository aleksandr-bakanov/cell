package bav.onecell.common.view

import android.content.Context
import android.graphics.Bitmap
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
import bav.onecell.model.hexes.Point
import kotlin.math.cos
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

    private val hexCorners: MutableList<Point> = mutableListOf()

    init {
        for (i in 0..5) hexCorners.add(Point())

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

    private var groundBitmap: Bitmap? = null
    private var groundShader: BitmapShader? = null
    fun setGroundShader(groundResourceId: Int) {
        groundBitmap?.let { if (!it.isRecycled) it.recycle() }
        groundBitmap = BitmapFactory.decodeResource(context.resources, groundResourceId)
        groundShader = BitmapShader(groundBitmap!!, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        groundPaint.shader = groundShader
    }

    class CellGraphicalPoints(
            var lifeHexes: MutableList<Path> = mutableListOf(),
            var lifeHexesIndex: Int = 0,

            var attackHexes: MutableList<Path> = mutableListOf(),
            var attackHexesIndex: Int = 0,

            var energyHexes: MutableList<Path> = mutableListOf(),
            var energyHexesIndex: Int = 0,

            var deathRayHexes: MutableList<Path> = mutableListOf(),
            var deathRayHexesIndex: Int = 0,

            var omniBulletHexes: MutableList<Path> = mutableListOf(),
            var omniBulletHexesIndex: Int = 0,

            var outline: MutableList<Point> = mutableListOf(),
            var outlineIndex: Int = 0,

            var isFriendly: Boolean = false) {

        fun getOutlinePoint(): Point {
            if (outline.size == outlineIndex) outline.add(Point())
            val p = outline[outlineIndex]
            outlineIndex++
            return p
        }

        fun getLifeHexPath(): Path {
            if (lifeHexes.size == lifeHexesIndex) lifeHexes.add(Path())
            val p = lifeHexes[lifeHexesIndex]
            lifeHexesIndex++
            p.reset()
            return p
        }
        
        fun getAttackHexPath(): Path {
            if (attackHexes.size == attackHexesIndex) attackHexes.add(Path())
            val p = attackHexes[attackHexesIndex]
            attackHexesIndex++
            p.reset()
            return p
        }

        fun getEnergyHexPath(): Path {
            if (energyHexes.size == energyHexesIndex) energyHexes.add(Path())
            val p = energyHexes[energyHexesIndex]
            energyHexesIndex++
            p.reset()
            return p
        }
        
        fun getDeathRayHexPath(): Path {
            if (deathRayHexes.size == deathRayHexesIndex) deathRayHexes.add(Path())
            val p = deathRayHexes[deathRayHexesIndex]
            deathRayHexesIndex++
            p.reset()
            return p
        }
        
        fun getOmniBulletHexPath(): Path {
            if (omniBulletHexes.size == omniBulletHexesIndex) omniBulletHexes.add(Path())
            val p = omniBulletHexes[omniBulletHexesIndex]
            omniBulletHexesIndex++
            p.reset()
            return p
        }

        fun reset() {
            lifeHexesIndex = 0
            attackHexesIndex = 0
            energyHexesIndex = 0
            deathRayHexesIndex = 0
            omniBulletHexesIndex = 0
            outlineIndex = 0
        }
    }

    fun getCellGraphicalRepresentation(cell: Cell, out: CellGraphicalPoints) {
        out.reset()
        if (cell.data.hexes.isEmpty()) return

        getCellOutline(cell, Layout.UNIT, out)

        hexMath.hexToPixel(Layout.UNIT, cell.data.origin, cellOrigin)
        for (hex in cell.data.hexes.values) {
            hexMath.add(hex, cell.data.origin, hexInGlobal)
            val path = when (hex.type) {
                Hex.Type.LIFE -> out.getLifeHexPath()
                Hex.Type.ENERGY -> out.getEnergyHexPath()
                Hex.Type.ATTACK -> out.getAttackHexPath()
                Hex.Type.DEATH_RAY -> out.getDeathRayHexPath()
                Hex.Type.OMNI_BULLET -> out.getOmniBulletHexPath()
                else -> null
            }

            var fadeScale = 1.0f
            cell.animationData.hexHashesToRemove?.let { hexHashesToRemove ->
                if (hexHashesToRemove.contains(hex.mapKey)) {
                    fadeScale = 1.0f - cell.animationData.fadeFraction
                }
            }

            getHexPath(Layout.UNIT, hexInGlobal, path!!, cellOrigin,
                       cell.animationData.rotation, cell.animationData.moveDirection,
                       cell.animationData.movingFraction, scale = fadeScale)
        }
        out.isFriendly = cell.data.groupId == Consts.MAIN_CHARACTERS_GROUP_ID
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
            for (i in 0 until graphics.outlineIndex step 2) {
                canvas?.drawLine((graphics.outline[i].x * layout.size.x + layout.origin.x).toFloat(),
                                 (graphics.outline[i].y * layout.size.y + layout.origin.y).toFloat(),
                                 (graphics.outline[i + 1].x * layout.size.x + layout.origin.x).toFloat(),
                                 (graphics.outline[i + 1].y * layout.size.y + layout.origin.y).toFloat(),
                                 if (graphics.isFriendly)
                                     groupAffiliationFriendPaint else groupAffiliationEnemyPaint)
            }
        }

        // Hexes
        for (i in 0 until graphics.lifeHexesIndex) {
            graphics.lifeHexes[i].transform(layoutMatrix, transformedHexPath)
            canvas?.drawPath(transformedHexPath, lPaint)
        }
        for (i in 0 until graphics.attackHexesIndex) {
            graphics.attackHexes[i].transform(layoutMatrix, transformedHexPath)
            canvas?.drawPath(transformedHexPath, aPaint)
        }
        for (i in 0 until graphics.energyHexesIndex) {
            graphics.energyHexes[i].transform(layoutMatrix, transformedHexPath)
            canvas?.drawPath(transformedHexPath, ePaint)
        }
        for (i in 0 until graphics.deathRayHexesIndex) {
            graphics.deathRayHexes[i].transform(layoutMatrix, transformedHexPath)
            canvas?.drawPath(transformedHexPath, dPaint)
        }
        for (i in 0 until graphics.omniBulletHexesIndex) {
            graphics.omniBulletHexes[i].transform(layoutMatrix, transformedHexPath)
            canvas?.drawPath(transformedHexPath, oPaint)
        }

        // Outline
        for (i in 0 until graphics.outlineIndex step 2) {
            canvas?.drawLine((graphics.outline[i].x * layout.size.x + layout.origin.x).toFloat(),
                             (graphics.outline[i].y * layout.size.y + layout.origin.y).toFloat(),
                             (graphics.outline[i + 1].x * layout.size.x + layout.origin.x).toFloat(),
                             (graphics.outline[i + 1].y * layout.size.y + layout.origin.y).toFloat(),
                             cellOutlinePaint)
        }
    }

    private val hexPath = Path()
    private val hexInGlobal = Hex()
    fun drawHexes(canvas: Canvas?, origin: Hex, hexes: Collection<Hex>?, paint: Paint, layout: Layout = Layout.DUMMY,
                  scale: Float = 1.0f) {
        hexes?.forEach { hex ->
            hexMath.add(hex, origin, hexInGlobal)
            getHexPath(layout, hexInGlobal, hexPath, scale = scale)
            hexPath.fillType = Path.FillType.EVEN_ODD
            canvas?.drawPath(hexPath, paint)
        }
    }

    fun getBulletPath(bullet: Bullet, out: Path, layout: Layout = Layout.DUMMY) {
        getHexPath(layout, bullet.origin, out, movingDirection = bullet.direction,
                          movingFraction = bullet.movingFraction, scale = 0.5f)
    }

    private fun getHexPath(layout: Layout, hex: Hex, out: Path, rotateAround: Point? = null, rotation: Float = 0f,
                           movingDirection: Int = 0, movingFraction: Float = 0f, scale: Float = 1f) {
        hexMath.polygonCorners(layout, hex, hexCorners, scale)

        // Rotation and moving offset
        rotateAround?.let { rotatePoints(hexCorners, it, rotation) }
        if (movingFraction > 0f) offsetPoints(hexCorners, movingDirection, movingFraction, layout)

        out.reset()
        out.moveTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        for (i in 1..(hexCorners.size - 1)) {
            out.lineTo(hexCorners[i].x.toFloat(), hexCorners[i].y.toFloat())
        }
        out.lineTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
    }

    private fun rotatePoints(points: List<Point>, origin: Point, angle: Float) {
        val s = sin(angle)
        val c = cos(angle)
        val size = points.size
        var i = 0
        var newX: Double
        var newY: Double
        while (i < size) {
            // Translate point back to origin
            points[i].x -= origin.x
            points[i].y -= origin.y
            // Rotate point
            newX = points[i].x * c - points[i].y * s
            newY = points[i].x * s + points[i].y * c
            // Translate point back
            points[i].x = newX + origin.x
            points[i].y = newY + origin.y
            i++
        }
    }

    private val offsetPoint = Point()
    private fun offsetPoints(points: List<Point>, direction: Int, fraction: Float, layout: Layout, point: Point? = null) {
        point?.let { offsetPoint.copy(it) } ?: hexMath.hexToPixel(layout, hexMath.getHexByDirection(direction), offsetPoint)
        val size = points.size
        var i = 0
        while (i < size) {
            points[i].x += (offsetPoint.x - layout.origin.x) * fraction
            points[i].y += (offsetPoint.y - layout.origin.y) * fraction
            i++
        }
    }

    fun offsetPoint(point: Point, direction: Int, fraction: Float, layout: Layout) {
        hexMath.hexToPixel(layout, hexMath.getHexByDirection(direction), offsetPoint)
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

    private val neighbor = Hex()
    private val cellOrigin = Point()
    private fun getCellOutline(cell: Cell, layout: Layout, out: CellGraphicalPoints) {
        hexMath.hexToPixel(layout, hexMath.getHexByDirection(cell.animationData.moveDirection), offsetPoint)
        hexMath.hexToPixel(layout, cell.data.origin, cellOrigin)

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
                        0 -> { out.getOutlinePoint().copy(hexCorners[4]); out.getOutlinePoint().copy(hexCorners[5]) }
                        1 -> { out.getOutlinePoint().copy(hexCorners[5]); out.getOutlinePoint().copy(hexCorners[0]) }
                        2 -> { out.getOutlinePoint().copy(hexCorners[0]); out.getOutlinePoint().copy(hexCorners[1]) }
                        3 -> { out.getOutlinePoint().copy(hexCorners[1]); out.getOutlinePoint().copy(hexCorners[2]) }
                        4 -> { out.getOutlinePoint().copy(hexCorners[2]); out.getOutlinePoint().copy(hexCorners[3]) }
                        5 -> { out.getOutlinePoint().copy(hexCorners[3]); out.getOutlinePoint().copy(hexCorners[4]) }
                        else -> Unit
                    }
                }
            }
        }
    }

    private val vPoint = Point()
    fun drawCellPower(canvas: Canvas?, cell: Cell, layout: Layout) {
        hexMath.hexToPixel(layout, cell.data.origin, cellOrigin)
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
            drawHexPower(canvas, layout, cell, hexInGlobal, hex.power, paint, vPoint, cellOrigin)
        }
    }

    private fun drawHexPower(canvas: Canvas?, layout: Layout, cell: Cell, hex: Hex, power: Int, paint: Paint, point: Point, cellOrigin: Point) {
        hexMath.hexToPixel(layout, hex, point)
        rotatePoint(point, cellOrigin, cell.animationData.rotation)
        offsetPoint(point, cell.animationData.moveDirection, cell.animationData.movingFraction, layout)
        canvas?.drawText(power.toString(), point.x.toFloat(), point.y.toFloat() + (layout.size.x / 3).toFloat(),
                         paint)
    }
}
