package bav.onecell.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import bav.onecell.R
import bav.onecell.common.view.CanvasView
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Point

class EditorCanvasView(context: Context, attributeSet: AttributeSet) : CanvasView(context, attributeSet) {

    companion object {
        private val TAG = "EditorCanvasView"
    }

    var cell: Cell? = null
    lateinit var presenter: Editor.Presenter
    var selectedCellType: Hex.Type = Hex.Type.LIFE
    var tipHexes: Collection<Hex>? = null
    val tappedHex: Hex = Hex()

    private val tipPaint = Paint()
    private val tipPaintLife = Paint()
    private val tipPaintAttack = Paint()
    private val tipPaintEnergy = Paint()
    private val tipPaintDeathRay = Paint()
    private val tipPaintOmniBullet = Paint()

    private val cellGraphics: DrawUtils.CellGraphicalPoints = DrawUtils.CellGraphicalPoints()

    init {
        for (p in arrayListOf(tipPaintLife, tipPaintAttack, tipPaintEnergy, tipPaintDeathRay, tipPaintOmniBullet)) {
            p.style = Paint.Style.STROKE
            p.strokeWidth = 10.0f
            p.strokeCap = Paint.Cap.ROUND
            p.strokeJoin = Paint.Join.ROUND
        }

        tipPaint.style = Paint.Style.FILL

        tipPaint.color = ContextCompat.getColor(context, R.color.cellEditorTip)
        tipPaintLife.color = ContextCompat.getColor(context, R.color.cellEditorTipLife)
        tipPaintAttack.color = ContextCompat.getColor(context, R.color.cellEditorTipAttack)
        tipPaintEnergy.color = ContextCompat.getColor(context, R.color.cellEditorTipEnergy)
        tipPaintDeathRay.color = ContextCompat.getColor(context, R.color.cellEditorTipDeathRay)
        tipPaintOmniBullet.color = ContextCompat.getColor(context, R.color.cellEditorTipOmniBullet)

        setOnTouchListener { view: View?, event: MotionEvent? ->
            super.onTouchListener(view, event)
            if (event?.action == MotionEvent.ACTION_UP) {
                if (!touchMoved) {
                    pointToHex(event.x, event.y, tappedHex)
                    Log.d(TAG, "tappedHex = $tappedHex")
                    if (selectedCellType == Hex.Type.REMOVE) {
                        Log.d(TAG, "remove tappedHex = $tappedHex")
                        presenter.removeHexFromCell(tappedHex)
                    } else {
                        tappedHex.type = selectedCellType
                        Log.d(TAG, "   add tappedHex = $tappedHex")
                        presenter.addHexToCell(tappedHex)
                    }
                    invalidate()
                }
                touchMoved = false
            }
            true
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        cell?.let {
            it.evaluateCellHexesPower()

            //canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), drawUtils.groundPaint)
            //drawBackgroundGrid(canvas)

            if (selectedCellType == Hex.Type.REMOVE) {
                drawUtils.drawCell(canvas, it, layout = layout)
                drawUtils.drawHexes(canvas, it.data.origin, tipHexes, getTipPaint(selectedCellType), layout)
            }
            else {
                drawUtils.drawHexes(canvas, it.data.origin, tipHexes, getTipPaint(selectedCellType), layout, scale = 0.7f)
                drawUtils.drawCell(canvas, it, layout = layout)
            }
            drawUtils.drawCellPower(canvas, it, layout)
        }
    }

    private fun getTipPaint(type: Hex.Type): Paint = when (type) {
        Hex.Type.LIFE -> tipPaintLife
        Hex.Type.ENERGY -> tipPaintEnergy
        Hex.Type.ATTACK -> tipPaintAttack
        Hex.Type.DEATH_RAY -> tipPaintDeathRay
        Hex.Type.OMNI_BULLET -> tipPaintOmniBullet
        else -> tipPaint
    }
}
