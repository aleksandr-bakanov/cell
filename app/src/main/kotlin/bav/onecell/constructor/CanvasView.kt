package bav.onecell.constructor

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import bav.onecell.model.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.Layout
import bav.onecell.model.hexes.Orientation
import bav.onecell.model.hexes.Point
import java.lang.Math.max
import java.lang.Math.min
import android.util.AttributeSet

class CanvasView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    companion object {
        private val TAG = "CanvasView"
    }

    var backgroundFieldRadius: Int = 1
        set(value) {
            backgroundHexes.clear()
            for (q in -value..value) {
                val r1 = max(-value, -q - value)
                val r2 = min(value, -q + value)
                for (r in r1..r2) {
                    backgroundHexes.add(Hex(q, r, -q - r))
                }
            }
            invalidate()
        }
    var cell: Cell? = null
    lateinit var presenter: Constructor.Presenter
    var selectedCellType: Hex.Type = Hex.Type.LIFE

    private val layout = Layout(
            Orientation.LAYOUT_POINTY,
            Point(100.0, 100.0), Point(100.0, 100.0))

    private val backgroundColor: Int = Color.LTGRAY
    private val gridPaint: Paint = Paint()
    private val lifePaint: Paint = Paint()
    private val energyPaint: Paint = Paint()
    private val attackPaint: Paint = Paint()
    private val strokePaint: Paint = Paint()
    private val darkStrokePaint: Paint = Paint()
    private val lightStrokePaint: Paint = Paint()

    private var isInitialized = false
    private val backgroundHexes: MutableSet<Hex> = mutableSetOf()

    init {
        gridPaint.style = Paint.Style.STROKE
        gridPaint.color = Color.GRAY
        gridPaint.strokeWidth = 5.0f

        lifePaint.style = Paint.Style.FILL
        lifePaint.color = Color.GREEN

        energyPaint.style = Paint.Style.FILL
        energyPaint.color = Color.YELLOW

        attackPaint.style = Paint.Style.FILL
        attackPaint.color = Color.RED

        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = Color.BLACK
        strokePaint.strokeWidth = 5.0f

        darkStrokePaint.style = Paint.Style.STROKE
        darkStrokePaint.color = Color.BLACK
        darkStrokePaint.strokeWidth = 5.0f

        lightStrokePaint.style = Paint.Style.STROKE
        lightStrokePaint.color = Color.WHITE
        lightStrokePaint.strokeWidth = 5.0f

        setOnTouchListener(
                { _: View?, event: MotionEvent? ->
                    if (event?.action == MotionEvent.ACTION_UP) {
                        val x: Double = event.x.toDouble()
                        val y: Double = event.y.toDouble()
                        val point = Point(x, y)
                        val fHex = Hex.pixelToHex(layout, point)
                        val hex = Hex.hexRound(fHex)
                        if (selectedCellType == Hex.Type.REMOVE) {
                            presenter.removeHexFromCell(hex)
                        }
                        else {
                            hex.type = selectedCellType
                            presenter.addHexToCell(hex)
                        }
                        invalidate()
                    }
                    true
                }
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        isInitialized = false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawColor(backgroundColor)

        initializeLayoutOrigin(canvas)
        drawBackgroundField(canvas)
        drawCell(canvas)
    }

    private fun drawCell(canvas: Canvas?) {
        cell?.let {
            var paint: Paint
            for (hex in it.hexes) {
                paint = when(hex.type) {
                    Hex.Type.LIFE -> lifePaint
                    Hex.Type.ENERGY -> energyPaint
                    Hex.Type.ATTACK -> attackPaint
                    else -> gridPaint
                }
                val path: Path = getHexPath(hex)
                path.fillType = Path.FillType.EVEN_ODD
                canvas?.drawPath(path, paint)
                canvas?.drawPath(path, strokePaint)
            }
        }

    }

    private fun initializeLayoutOrigin(canvas: Canvas?) {
        if (!isInitialized) {
            layout.origin = Point(canvas!!.width.toDouble() / 2.0,
                    canvas.height.toDouble() / 2.0)
            isInitialized = true
        }
    }

    private fun drawBackgroundField(canvas: Canvas?) {
        for (hex in backgroundHexes) {
            canvas?.drawPath(getHexPath(hex), gridPaint)
        }
    }

    private fun getHexPath(hex: Hex): Path {
        val hexCorners: ArrayList<Point> = Hex.poligonCorners(layout, hex)
        val path = Path()
        path.moveTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        for (i in 1..(hexCorners.size - 1)) {
            path.lineTo(hexCorners[i].x.toFloat(), hexCorners[i].y.toFloat())
        }
        path.lineTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        return path
    }

    private fun getLightHexPath(hex: Hex): Path {
        return getPartialHexPath(hex, 0, 3)
    }

    private fun getDarkHexPath(hex: Hex): Path {
        return getPartialHexPath(hex, 3, 5, true)
    }

    private fun getPartialHexPath(hex: Hex, startCorner: Int, endCorner: Int, endInFirstCorner: Boolean = false): Path {
        val hexCorners: ArrayList<Point> = Hex.poligonCorners(layout, hex)
        val path = Path()
        path.moveTo(hexCorners[startCorner].x.toFloat(), hexCorners[startCorner].y.toFloat())
        for (i in (startCorner + 1)..endCorner) {
            path.lineTo(hexCorners[i].x.toFloat(), hexCorners[i].y.toFloat())
        }
        if (endInFirstCorner) path.lineTo(hexCorners[0].x.toFloat(), hexCorners[0].y.toFloat())
        return path
    }

}
