package bav.onecell.battle

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
import bav.onecell.common.view.CanvasView
import bav.onecell.model.BattleFieldSnapshot
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex

class BattleCanvasView(context: Context, attributeSet: AttributeSet) : CanvasView(context, attributeSet) {

    companion object {
        private const val TAG = "BattleCanvasView"
    }

    lateinit var presenter: Battle.Presenter
    var ring = listOf<Hex>()
    private val ringPaint = Paint()
    private val corpseLifePaint = Paint()
    private val corpseEnergyPaint = Paint()
    private val corpseAttackPaint = Paint()
    private val corpseDeathRayHexPaint = Paint()
    private val groundPaint = Paint()
    private val clipPath = Path()
    var snapshots: List<BattleFieldSnapshot>? = null
    var currentSnapshotIndex: Int = 0
    var fallBackToPreviousSnapshot = false
    var isFog: Boolean = false
    var deathRayFraction: Float = 0f

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

        groundPaint.style = Paint.Style.FILL
        groundPaint.color = ContextCompat.getColor(context, R.color.battleViewGround)

        setOnTouchListener { view: View?, event: MotionEvent? -> super.onTouchListener(view, event) }
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
                                       corpseAttackPaint, corpseDeathRayHexPaint, layout)
                }
                snapshot.cells.forEach { cell ->
                    drawUtils.drawCell(canvas, cell, layout = layout)
                    drawUtils.drawCellPower(canvas, cell, layout)
                }
                drawUtils.drawDeathRays(canvas, snapshot.deathRays, deathRayFraction, layout)
            }
        }
        if (fallBackToPreviousSnapshot) {
            currentSnapshotIndex--
            fallBackToPreviousSnapshot = false
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

    override fun decreaseLayoutSize() {
        super.decreaseLayoutSize()
    }

    override fun increaseLayoutSize() {
        super.increaseLayoutSize()
    }
}
