package bav.onecell.battle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import bav.onecell.R
import bav.onecell.common.view.CanvasView
import bav.onecell.model.battle.FrameGraphics
import bav.onecell.model.hexes.Point
import java.lang.IllegalArgumentException
import kotlin.math.max
import kotlin.math.min

class BattleCanvasView(context: Context, attributeSet: AttributeSet) : CanvasView(context, attributeSet) {

    companion object {
        private const val TAG = "BattleCanvasView"
        const val MIN_SCALE = 4f
        const val MAX_SCALE = 200.0f
    }

    lateinit var presenter: Battle.Presenter
    private val corpseLifePaint = Paint()
    private val corpseEnergyPaint = Paint()
    private val corpseAttackPaint = Paint()
    private val corpseDeathRayHexPaint = Paint()
    private val corpseOmniBulletHexPaint = Paint()
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener(this))
    var scaleFactor: Float = 1f
    var frames: Map<Long, FrameGraphics>? = null
    var currentFrameGraphics: FrameGraphics? = null
    var transformedPath: Path = Path()

    init {
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
                        updateLayoutMatrix()
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

    override fun modifyScaleFactor(factor: Float) {
        scaleFactor = factor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scaleFactor = layout.size.x.toFloat()
    }

    fun drawFrame(timestamp: Long) {
        frames?.get(timestamp)?.let { frameGraphics ->
            currentFrameGraphics = frameGraphics
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        currentFrameGraphics?.let { graphics ->
            // Draw fog
            if (graphics.fieldOfView != null) {
                canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), drawUtils.groundPaint)
                canvas?.drawColor(0x77000000)
                graphics.fieldOfView?.transform(layoutMatrix, transformedPath)
                canvas?.clipPath(transformedPath)
            } else {
                canvas?.clipRect(0f, 0f, width.toFloat(), height.toFloat())
            }

            // Background
            canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), drawUtils.groundPaint)

            // Corpses
            if (!graphics.corpses.isNullOrEmpty()) {
                for (corpse in graphics.corpses!!) {
                    drawUtils.drawCellGraphicalRepresentation(canvas, corpse, layout, layoutMatrix,
                                                              corpseLifePaint, corpseEnergyPaint, corpseAttackPaint,
                                                              corpseDeathRayHexPaint, corpseOmniBulletHexPaint, true)
                }
            }

            // Living cells
            if (!graphics.livingCells.isNullOrEmpty()) {
                for (cell in graphics.livingCells!!) {
                    drawUtils.drawCellGraphicalRepresentation(canvas, cell, layout, layoutMatrix,
                                                              drawUtils.lifePaint, drawUtils.energyPaint, drawUtils.attackPaint,
                                                              drawUtils.deathRayHexPaint, drawUtils.omniBulletHexPaint, false)
                }
            }

            // Death rays
            graphics.deathRays?.let { points ->
                drawUtils.deathRayPaint.alpha = graphics.deathRaysAlpha
                for (i in 0 until points.size step 2) {
                    canvas?.drawLine((points[i].x * layout.size.x + layout.origin.x).toFloat(),
                                     (points[i].y * layout.size.y + layout.origin.y).toFloat(),
                                     (points[i + 1].x * layout.size.x + layout.origin.x).toFloat(),
                                     (points[i + 1].y * layout.size.y + layout.origin.y).toFloat(),
                                     drawUtils.deathRayPaint)
                }
            }

            // Bullets
            graphics.bullets?.let { paths ->
                for (path in paths) {
                    path.transform(layoutMatrix, transformedPath)
                    canvas?.drawPath(transformedPath, drawUtils.omniBulletHexPaint)
                    canvas?.drawPath(transformedPath, drawUtils.cellOutlinePaint)
                }
            }
        }
    }

    private class ScaleListener(private val view: BattleCanvasView): ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            val factor = detector?.scaleFactor ?: 1f

            view.scaleFactor *= factor
            view.scaleFactor = max(MIN_SCALE, min(view.scaleFactor, MAX_SCALE))
            view.setLayoutSize(view.scaleFactor.toDouble())

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
            view.updateLayoutMatrix()

            view.invalidate()
            return true
        }
    }
}
