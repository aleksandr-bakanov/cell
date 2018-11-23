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

    private val tipPaint = Paint()
    private val tipPaintLife = Paint()
    private val tipPaintAttack = Paint()
    private val tipPaintEnergy = Paint()
    private val tipPaintDeathRay = Paint()
    private val tipPaintOmniBullet = Paint()

    init {
        for (p in arrayListOf(tipPaint, tipPaintLife, tipPaintAttack, tipPaintEnergy, tipPaintDeathRay, tipPaintOmniBullet))
            p.style = Paint.Style.FILL

        tipPaint.color = ContextCompat.getColor(context, R.color.cellEditorTip)
        tipPaintLife.color = ContextCompat.getColor(context, R.color.cellEditorTipLife)
        tipPaintAttack.color = ContextCompat.getColor(context, R.color.cellEditorTipAttack)
        tipPaintEnergy.color = ContextCompat.getColor(context, R.color.cellEditorTipEnergy)
        tipPaintDeathRay.color = ContextCompat.getColor(context, R.color.cellEditorTipDeathRay)
        tipPaintOmniBullet.color = ContextCompat.getColor(context, R.color.cellEditorTipOmniBullet)

        setOnTouchListener { view: View?, event: MotionEvent? ->
            super.onTouchListener(view, event)
            if (event?.action == MotionEvent.ACTION_UP) {
                if (event.pointerCount == 1 && !touchMoved) {
                    val hex = pointToHex(event.x, event.y)
                    if (selectedCellType == Hex.Type.REMOVE) {
                        presenter.removeHexFromCell(hex)
                    } else {
                        hex.type = selectedCellType
                        presenter.addHexToCell(hex)
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
            if (selectedCellType == Hex.Type.REMOVE) {
                drawUtils.drawCell(canvas, it, layout = layout)
                drawUtils.drawHexes(canvas, it.data.origin, tipHexes, getTipPaint(selectedCellType), layout)
            }
            else {
                drawUtils.drawHexes(canvas, it.data.origin, tipHexes, getTipPaint(selectedCellType), layout)
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

    fun pointToHex(x: Float, y: Float): Hex {
        val point = Point(x.toDouble(), y.toDouble())
        val fHex = hexMath.pixelToHex(layout, point)
        return hexMath.round(fHex)
    }
}
