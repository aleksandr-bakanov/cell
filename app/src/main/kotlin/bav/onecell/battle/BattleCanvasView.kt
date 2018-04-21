package bav.onecell.battle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import bav.onecell.R
import bav.onecell.common.view.CanvasView
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex

class BattleCanvasView(context: Context, attributeSet: AttributeSet) : CanvasView(context, attributeSet) {

    companion object {
        private const val TAG = "BattleCanvasView"
    }

    lateinit var presenter: Battle.Presenter
    var ring = listOf<Hex>()
    private val ringPaint = Paint()
    private val corpsePaint = Paint()
    lateinit var cells: List<Cell>
    lateinit var corpses: List<Cell>

    init {
        ringPaint.style = Paint.Style.FILL
        ringPaint.color = ContextCompat.getColor(context, R.color.battleViewRing)

        corpsePaint.style = Paint.Style.FILL
        corpsePaint.color = ContextCompat.getColor(context, R.color.battleViewCorpse)

        setOnTouchListener(
                { view: View?, event: MotionEvent? ->
                    super.onTouchListener(view, event)
                }
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawRing(canvas)
        corpses.forEach { corpse -> drawCell(canvas, corpse, corpsePaint, corpsePaint, corpsePaint) }
        cells.forEach { cell -> drawCell(canvas, cell) }
    }

    private fun drawRing(canvas: Canvas?) {
        ring.forEach {
            val path: Path = getHexPath(it)
            path.fillType = Path.FillType.EVEN_ODD
            canvas?.drawPath(path, ringPaint)
        }
    }

}