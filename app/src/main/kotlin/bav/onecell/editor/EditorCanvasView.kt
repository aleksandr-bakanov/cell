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
import bav.onecell.common.Common
import bav.onecell.common.view.CanvasView
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Point

class EditorCanvasView(context: Context, attributeSet: AttributeSet) : CanvasView(context, attributeSet) {

    companion object {
        private val TAG = "EditorCanvasView"
        private const val CELL_CENTER_MARKER_SIZE = 24f
    }

    var cell: Cell? = null
    lateinit var presenter: Editor.Presenter
    var selectedCellType: Hex.Type = Hex.Type.LIFE
    var tipHexes: Collection<Hex>? = null
    private val tappedHex: Hex = Hex()

    private var cellRepresentation: DrawUtils.CellGraphicalPoints? = null
    lateinit var objectPool: Common.ObjectPool

    private val tipPaint = Paint()
    private val tipPaintLife = Paint()
    private val tipPaintAttack = Paint()
    private val tipPaintEnergy = Paint()
    private val tipPaintDeathRay = Paint()
    private val tipPaintOmniBullet = Paint()
    private val cellCenterMarkerPaint = Paint()

    init {
        for (p in arrayListOf(tipPaintLife, tipPaintAttack, tipPaintEnergy, tipPaintDeathRay, tipPaintOmniBullet)) {
            p.style = Paint.Style.STROKE
            p.strokeWidth = 10f
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

        cellCenterMarkerPaint.style = Paint.Style.STROKE
        cellCenterMarkerPaint.strokeWidth = 3f
        cellCenterMarkerPaint.strokeCap = Paint.Cap.ROUND
        cellCenterMarkerPaint.color = ContextCompat.getColor(context, R.color.cellEditorCenterMarker)

        setOnTouchListener { view: View?, event: MotionEvent? ->
            super.onTouchListener(view, event)
            if (event?.action == MotionEvent.ACTION_UP) {
                if (!touchMoved) {
                    pointToHex(event.x, event.y, tappedHex)
                    if (selectedCellType == Hex.Type.REMOVE) {
                        presenter.removeHexFromCell(tappedHex)
                    } else {
                        tappedHex.type = selectedCellType
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
            if (selectedCellType == Hex.Type.REMOVE) {
                cellRepresentation?.let { graphics ->
                    drawUtils.drawCellGraphicalRepresentation(canvas, graphics, layout, layoutMatrix, isCorpse = false)
                }
                drawUtils.drawHexes(canvas, it.data.origin, tipHexes, getTipPaint(selectedCellType), layout)
            }
            else {
                drawUtils.drawHexes(canvas, it.data.origin, tipHexes, getTipPaint(selectedCellType), layout, scale = 0.7f)
                cellRepresentation?.let { graphics ->
                    drawUtils.drawCellGraphicalRepresentation(canvas, graphics, layout, layoutMatrix, isCorpse = false)
                }
            }
            drawUtils.drawCellPower(canvas, it, layout)
        }
        drawCellCenterMarker(canvas)
    }

    fun updateCellRepresentation() {
        cell?.let {
            cellRepresentation = objectPool.getCellGraphicalRepresentation()
            drawUtils.getCellGraphicalRepresentation(it, cellRepresentation!!)
        }
    }

    private fun drawCellCenterMarker(canvas: Canvas?) {
        canvas?.let {
            // Horizontal
            /*it.drawLine((layout.origin.x - CELL_CENTER_MARKER_SIZE).toFloat(), (layout.origin.y).toFloat(),
                        (layout.origin.x + CELL_CENTER_MARKER_SIZE).toFloat(), (layout.origin.y).toFloat(),
                        cellCenterMarkerPaint)
            // Vertical
            it.drawLine((layout.origin.x).toFloat(), (layout.origin.y - CELL_CENTER_MARKER_SIZE).toFloat(),
                        (layout.origin.x).toFloat(), (layout.origin.y + CELL_CENTER_MARKER_SIZE).toFloat(),
                        cellCenterMarkerPaint)*/
            // Circle
            it.drawCircle(layout.origin.x.toFloat(), layout.origin.y.toFloat(), CELL_CENTER_MARKER_SIZE, cellCenterMarkerPaint)
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
