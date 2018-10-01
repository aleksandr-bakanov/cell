package bav.onecell.battle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import bav.onecell.R
import bav.onecell.common.view.CanvasView
import bav.onecell.model.BattleFieldSnapshot
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
    var snapshots: List<BattleFieldSnapshot>? = null
    var currentSnapshotIndex: Int = 0
    var fallBackToPreviousSnapshot = false

    init {
        ringPaint.style = Paint.Style.FILL
        ringPaint.color = ContextCompat.getColor(context, R.color.battleViewRing)

        corpseLifePaint.style = Paint.Style.FILL
        corpseLifePaint.color = ContextCompat.getColor(context, R.color.battleViewCorpseLife)

        corpseEnergyPaint.style = Paint.Style.FILL
        corpseEnergyPaint.color = ContextCompat.getColor(context, R.color.battleViewCorpseEnergy)

        corpseAttackPaint.style = Paint.Style.FILL
        corpseAttackPaint.color = ContextCompat.getColor(context, R.color.battleViewCorpseAttack)

        setOnTouchListener(
                { view: View?, event: MotionEvent? ->
                    super.onTouchListener(view, event)
                }
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        snapshots?.let {
            if (currentSnapshotIndex >= 0 && currentSnapshotIndex < it.size) {
                val snapshot = it[currentSnapshotIndex]
                snapshot.corpses.forEach { corpse ->
                    drawUtils.drawCell(canvas, corpse, corpseLifePaint, corpseEnergyPaint, corpseAttackPaint, layout)
                }
                snapshot.cells.forEach { cell ->
                    drawUtils.drawCell(canvas, cell, layout = layout)
                    drawUtils.drawCellPower(canvas, cell, layout)
                }
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

}
