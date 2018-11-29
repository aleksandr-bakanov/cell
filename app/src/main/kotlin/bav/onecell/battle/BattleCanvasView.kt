package bav.onecell.battle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import bav.onecell.R
import bav.onecell.common.view.CanvasView
import bav.onecell.model.BattleFieldSnapshot
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Layout
import bav.onecell.model.hexes.Point
import java.lang.IllegalArgumentException
import kotlin.math.max
import kotlin.math.min

class BattleCanvasView(context: Context, attributeSet: AttributeSet) : CanvasView(context, attributeSet) {

    companion object {
        private const val TAG = "BattleCanvasView"
        const val MIN_SCALE = 0.01f
        const val MAX_SCALE = 50.0f
    }

    lateinit var presenter: Battle.Presenter
    var ring = listOf<Hex>()
    private val ringPaint = Paint()
    private val corpseLifePaint = Paint()
    private val corpseEnergyPaint = Paint()
    private val corpseAttackPaint = Paint()
    private val corpseDeathRayHexPaint = Paint()
    private val corpseOmniBulletHexPaint = Paint()
    private val groundPaint = Paint()
    private val clipPath = Path()
    var snapshots: List<BattleFieldSnapshot>? = null
    var currentSnapshotIndex: Int = 0
    var isFog: Boolean = false
    var deathRayFraction: Float = 0f
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener(this))
    var scaleFactor: Float = 1f

    init {
        ringPaint.style = Paint.Style.FILL
        ringPaint.color = ContextCompat.getColor(context, R.color.battleViewRing)

        corpseLifePaint.style = Paint.Style.FILL
        corpseLifePaint.color = ContextCompat.getColor(context, R.color.battleViewCorpseLife)

        corpseEnergyPaint.style = Paint.Style.FILL
        corpseEnergyPaint.color = ContextCompat.getColor(context, R.color.battleViewCorpseEnergy)

        corpseAttackPaint.style = Paint.Style.FILL
        corpseAttackPaint.color = ContextCompat.getColor(context, R.color.battleViewCorpseAttack)

        corpseDeathRayHexPaint.style = Paint.Style.FILL
        corpseDeathRayHexPaint.color = ContextCompat.getColor(context, R.color.battleViewCorpseDeathRay)

        corpseOmniBulletHexPaint.style = Paint.Style.FILL
        corpseOmniBulletHexPaint.color = ContextCompat.getColor(context, R.color.battleViewCorpseOmniBullet)

        groundPaint.style = Paint.Style.FILL
        groundPaint.color = ContextCompat.getColor(context, R.color.battleViewGround)

        setOnTouchListener { view: View?, event: MotionEvent? ->
            var ret = false
            event?.let {
                try {
                    if (it.pointerCount == 2) {
                        if (it.actionMasked == MotionEvent.ACTION_POINTER_UP) {
                            lastTouchX = it.getX(it.getPointerId(0))
                            lastTouchY = it.getY(it.getPointerId(0))
                        }
                        scaleGestureDetector.onTouchEvent(it)
                    } else if (it.action == MotionEvent.ACTION_DOWN) {
                        lastTouchX = it.getX(it.getPointerId(0))
                        lastTouchY = it.getY(it.getPointerId(0))
                        ret = true
                    } else if (it.action == MotionEvent.ACTION_MOVE && it.pointerCount == 1) {
                        val curX = it.getX(it.getPointerId(0))
                        val curY = it.getY(it.getPointerId(0))
                        val dx = curX - lastTouchX
                        val dy = curY - lastTouchY
                        lastTouchX = curX
                        lastTouchY = curY
                        layout.origin = Point(layout.origin.x + dx, layout.origin.y + dy)
                        ret = true
                        invalidate()
                    } else if (it.action == MotionEvent.ACTION_UP) {
                        lastTouchX = it.getX(it.getPointerId(0))
                        lastTouchY = it.getY(it.getPointerId(0))
                        ret = true
                    }
                    else {}
                } catch (e: IllegalArgumentException) {
                    // Just ignore exception caused by ScaleGestureDetector
                    // See details here:
                    // https://github.com/chrisbanes/PhotoView/issues/31
                    // https://github.com/chrisbanes/PhotoView/commit/92a2a281134ceddc6e402ba4a83cc91180db8115#comments
                    // TODO: deal with 'pointerIndex out of range' exception lately
                }
            }
            ret
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        snapshots?.let {
            if (currentSnapshotIndex >= 0 && currentSnapshotIndex < it.size) {
                val snapshot = it[currentSnapshotIndex]
                // Draw fog
                if (isFog) {
                    canvas?.drawColor(Color.DKGRAY)
                    canvas?.clipPath(observableAreaToPath(getObservableArea(snapshot.cells)))
                }
                canvas?.drawColor(groundPaint.color)
                snapshot.corpses.forEach { corpse ->
                    drawUtils.drawCell(canvas, corpse, corpseLifePaint, corpseEnergyPaint,
                                       corpseAttackPaint, corpseDeathRayHexPaint, corpseOmniBulletHexPaint, layout)
                }
                snapshot.cells.forEach { cell ->
                    drawUtils.drawCell(canvas, cell, layout = layout)
                    //drawUtils.drawCellPower(canvas, cell, layout)
                }
                drawUtils.drawDeathRays(canvas, snapshot.deathRays, deathRayFraction, layout)

                snapshot.bullets.forEach { bullet -> drawUtils.drawBullet(canvas, bullet, layout = layout) }

                // Layout center
                /*canvas?.drawCircle(layout.origin.x.toFloat(), layout.origin.y.toFloat(), 5f, ringPaint)
                canvas?.let { c ->
                    c.drawLine((width / 2 - 50).toFloat(), (height / 2).toFloat(), (width / 2 + 50).toFloat(), (height / 2).toFloat(), drawUtils.strokePaint)
                    c.drawLine((width / 2).toFloat(), (height / 2 - 50).toFloat(), (width / 2).toFloat(), (height / 2 + 50).toFloat(), drawUtils.strokePaint)
                }*/
            }
        }
    }

    private fun drawRing(canvas: Canvas?) {
        ring.forEach {
            val path: Path = getHexPath(it)
            path.fillType = Path.FillType.EVEN_ODD
            canvas?.drawPath(path, ringPaint)
        }
    }

    private fun getObservableArea(cells: List<Cell>): Set<Hex> {
        // Get area observed by cells with group id = 0 only, i.e. main heroes
        val commonArea = mutableSetOf<Hex>()
        cells.forEach { cell ->
            if (cell.data.groupId == 0) {
                val cellViewArea = mutableSetOf<Hex>()
                cell.data.hexes.values.forEach { hex -> cellViewArea.add(hexMath.add(hex, cell.data.origin)) }
                for (i in 0 until cell.data.viewDistance) {
                    val nextLayer = mutableSetOf<Hex>()
                    cellViewArea.forEach { hex ->
                        nextLayer.addAll(hexMath.hexNeighbors(hex).subtract(cellViewArea))
                    }
                    cellViewArea.addAll(nextLayer)
                }
                commonArea.addAll(cellViewArea)
            }
        }
        return commonArea
    }

    private fun observableAreaToPath(hexes: Collection<Hex>): Path {
        clipPath.reset()
        hexes.forEach { hex ->
            val origin = hexMath.hexToPixel(layout, hex)
            clipPath.addCircle(origin.x.toFloat(), origin.y.toFloat(), layout.size.x.toFloat(), Path.Direction.CW)
        }
        clipPath.close()
        return clipPath
    }

    private class ScaleListener(private val view: BattleCanvasView): ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            val factor = detector?.scaleFactor ?: 1f

            view.scaleFactor *= factor
            view.scaleFactor = max(MIN_SCALE, min(view.scaleFactor, MAX_SCALE))
            view.setLayoutSize(view.scaleFactor.toDouble() * Layout.DUMMY.size.x)

            var layoutX = view.layout.origin.x
            var layoutY = view.layout.origin.y
            layoutX -= view.width / 2
            layoutY -= view.height / 2
            layoutX *= factor
            layoutY *= factor
            layoutX += view.width / 2
            layoutY += view.height / 2
            view.layout.origin.x = layoutX
            view.layout.origin.y = layoutY

            view.invalidate()
            return true
        }
    }
}
