package bav.onecell.model.hexes

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

data class Hex(val q: Int, val r: Int, val s: Int) {

    enum class Type {
        LIFE, ENERGY, ATTACK, REMOVE
    }

    companion object {

        private val HEX_DIRECTIONS: Array<Hex> = arrayOf(
                Hex(1, 0, -1), Hex(1, -1, 0),
                Hex(0, -1, 1),
                Hex(-1, 0, 1), Hex(-1, 1, 0),
                Hex(0, 1, -1)
        )

        fun hexDirection(direction: Int /* 0 to 5 */): Hex {
            assert(direction in 0..5)
            return HEX_DIRECTIONS[direction]
        }

        fun hexNeighbor(hex: Hex, direction: Int): Hex {
            return hexAdd(hex, hexDirection(direction))
        }

        fun hexNeighbors(hex: Hex): MutableSet<Hex> {
            return mutableSetOf(
                    hexNeighbor(hex, 0), hexNeighbor(hex, 1),
                    hexNeighbor(hex, 2), hexNeighbor(hex, 3),
                    hexNeighbor(hex, 4), hexNeighbor(hex, 5))
        }

        fun hexAdd(a: Hex, b: Hex): Hex {
            return Hex(a.q + b.q, a.r + b.r, a.s + b.s)
        }

        fun hexSubstract(a: Hex, b: Hex): Hex {
            return Hex(a.q - b.q, a.r - b.r, a.s - b.s)
        }

        fun hexMultiply(a: Hex, k: Int): Hex {
            return Hex(a.q * k, a.r * k, a.s * k)
        }

        fun hexLength(hex: Hex): Int {
            return (abs(hex.q) + abs(hex.r) + abs(hex.s) / 2)
        }

        fun hexDistance(a: Hex, b: Hex): Int {
            return hexLength(
                    hexSubstract(a, b))
        }

        fun hexToPixel(layout: Layout, h: Hex): Point {
            val m: Orientation = layout.orientation
            val x: Double = (m.f0 * h.q + m.f1 * h.r) * layout.size.x
            val y: Double = (m.f2 * h.q + m.f3 * h.r) * layout.size.y
            return Point(x + layout.origin.x, y + layout.origin.y)
        }

        fun pixelToHex(layout: Layout, p: Point): FractionalHex {
            val m: Orientation = layout.orientation
            val pt: Point = Point(
                    (p.x - layout.origin.x) / layout.size.x,
                    (p.y - layout.origin.y) / layout.size.y)
            val q: Double = m.b0 * pt.x + m.b1 * pt.y
            val r: Double = m.b2 * pt.x + m.b3 * pt.y
            return FractionalHex(q, r, -q - r)
        }

        fun hexCornerOffset(layout: Layout, corner: Int): Point {
            val size: Point = layout.size
            val angle: Double = 2.0 * PI * (layout.orientation.startAngle + corner) / 6.0
            return Point(size.x * cos(angle), size.y * sin(angle))
        }

        fun poligonCorners(layout: Layout, h: Hex): ArrayList<Point> {
            val corners: ArrayList<Point> = arrayListOf()
            val center: Point = hexToPixel(layout, h)
            for (i in 0..5) {
                val offset: Point = hexCornerOffset(layout, i)
                corners.add(Point(center.x + offset.x, center.y + offset.y))
            }
            return corners
        }

        fun hexRound(h: FractionalHex): Hex {
            var q: Int = h.q.roundToInt()
            var r: Int = h.r.roundToInt()
            var s: Int = h.s.roundToInt()
            val qDiff: Double = abs(q - h.q)
            val rDiff: Double = abs(r - h.r)
            val sDiff: Double = abs(s - h.s)
            if (qDiff > rDiff && qDiff > sDiff) {
                q = -r - s
            } else if (rDiff > sDiff) {
                r = -q - s
            } else {
                s = -q - r
            }
            return Hex(q, r, s)
        }

        fun lerp(a: Double, b: Double, t: Double): Double {
            return a * (1 - t) + b * t
        }

        fun hexLerp(a: FractionalHex, b: FractionalHex, t: Double): FractionalHex {
            return FractionalHex(
                    lerp(a.q, b.q, t),
                    lerp(a.r, b.r, t),
                    lerp(a.s, b.s, t))
        }

        fun hexLineDraw(a: Hex, b: Hex): ArrayList<Hex> {
            val n: Int = hexDistance(a, b)
            val aNudge = FractionalHex(a.q + 1e-6, a.r + 1e-6, a.s - 2e-6)
            val bNudge = FractionalHex(b.q + 1e-6, b.r + 1e-6, b.s - 2e-6)
            val results: ArrayList<Hex> = arrayListOf()
            val step: Double = 1.0 / max(n, 1)
            for (i in 0..n) {
                results.add(hexRound(
                        hexLerp(aNudge, bNudge, step * i)))
            }
            return results
        }
    }

    var type: Type = Type.REMOVE

    init {
        assert(q + r + s == 0)
    }

    fun withType(type: Type): Hex {
        this.type = type
        return this
    }

}