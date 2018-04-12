package bav.onecell.model.hexes

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

class HexMath {

    val ZERO_HEX = Hex(0, 0, 0)

    private val DIRECTIONS: Array<Hex> = arrayOf(
            Hex(1, -1, 0), // 0
            Hex(1, 0, -1), // 1
            Hex(0, 1, -1), // 2
            Hex(-1, 1, 0), // 3
            Hex(-1, 0, 1), // 4
            Hex(0, -1, 1)  // 5
    )

    fun getHexByDirection(direction: Int /* 0 to 5 */): Hex {
        if (direction < 0 || direction > 5) throw IllegalArgumentException("Direction should be in range [0..5]")
        return DIRECTIONS[direction]
    }

    fun getNeighborsWithinRadius(origin: Hex, radius: Int): Set<Hex> {
        if (radius <= 0) throw IllegalArgumentException("Radius should be more than zero")
        val hexes = mutableSetOf<Hex>()
        for (q in -radius..radius) {
            val r1 = Math.max(-radius, -q - radius)
            val r2 = Math.min(radius, -q + radius)
            for (r in r1..r2) {
                hexes.add(add(origin, Hex(q, r, -q - r)))
            }
        }
        hexes.remove(origin)
        return hexes
    }

    /**
     * Convert angle in radians to direction
     * TODO: make it formula
     *
     * @param angle Angle in radians, should be between -PI and PI
     * @return Corresponding direction
     */
    fun radToDir(angle: Float): Int {
        if (angle < -PI.toFloat() || angle > PI.toFloat()) throw IllegalArgumentException("Angle should be in range [-PI..PI]")
        return if (angle >= (-PI * 5 / 6) && angle < (-PI / 2)) 5
        else if (angle >= (-PI / 2) && angle < (-PI / 6)) 0
        else if (angle >= (-PI / 6) && angle < (PI / 6)) 1
        else if (angle >= (PI / 6) && angle < (PI / 2)) 2
        else if (angle >= (PI / 2) && angle < (PI * 5 / 6)) 3
        else 4
    }

    fun getHexNeighbor(hex: Hex, direction: Int): Hex {
        return add(hex, getHexByDirection(direction))
    }

    //  5    0
    //    /\
    // 4 |  | 1
    //    \/
    //  3    2
    fun hexNeighbors(hex: Hex): Collection<Hex> {
        return mutableSetOf(
                getHexNeighbor(hex, 0), getHexNeighbor(hex, 1),
                getHexNeighbor(hex, 2), getHexNeighbor(hex, 3),
                getHexNeighbor(hex, 4), getHexNeighbor(hex, 5))
    }

    fun add(a: Hex, b: Hex): Hex {
        return Hex(a.q + b.q, a.r + b.r, a.s + b.s)
    }

    fun subtract(a: Hex, b: Hex): Hex {
        return Hex(a.q - b.q, a.r - b.r, a.s - b.s)
    }

    fun multiply(a: Hex, k: Int): Hex {
        return Hex(a.q * k, a.r * k, a.s * k)
    }

    fun length(hex: Hex): Int {
        return (abs(hex.q) + abs(hex.r) + abs(hex.s) / 2)
    }

    fun distance(a: Hex, b: Hex): Int {
        return (abs(a.q - b.q) + abs(a.r - b.r) + abs(a.s - b.s)) / 2
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

    //   4
    // 3 /\ 5
    //  |  |
    // 2 \/ 0
    //   1
    fun poligonCorners(layout: Layout, h: Hex): ArrayList<Point> {
        val corners: ArrayList<Point> = arrayListOf()
        val center: Point = hexToPixel(layout, h)
        for (i in 0..5) {
            val offset: Point = hexCornerOffset(layout, i)
            corners.add(Point(center.x + offset.x, center.y + offset.y))
        }
        return corners
    }

    fun round(h: FractionalHex): Hex {
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

    fun lineDraw(a: Hex, b: Hex): ArrayList<Hex> {
        val n: Int = distance(a, b)
        val aNudge = FractionalHex(a.q + 1e-6, a.r + 1e-6, a.s - 2e-6)
        val bNudge = FractionalHex(b.q + 1e-6, b.r + 1e-6, b.s - 2e-6)
        val results: ArrayList<Hex> = arrayListOf()
        val step: Double = 1.0 / max(n, 1)
        for (i in 0..n) {
            results.add(round(
                    hexLerp(aNudge, bNudge, step * i)))
        }
        return results
    }

    /**
     * Rotate hex to 60 degrees CW relative to (0, 0, 0)
     */
    fun rotateRight(h: Hex): Hex {
        return Hex(-h.r, -h.s, -h.q)
    }

    /**
     * Rotate hex to 60 degrees CCW relative to (0, 0, 0)
     */
    fun rotateLeft(h: Hex): Hex {
        return Hex(-h.s, -h.q, -h.r)
    }

    /**
     * Rotate hex to 120 degrees CW relative to (0, 0, 0)
     */
    fun rotateRightTwice(h: Hex): Hex {
        return Hex(h.s, h.q, h.r)
    }

    /**
     * Rotate hex to 120 degrees CCW relative to (0, 0, 0)
     */
    fun rotateLeftTwice(h: Hex): Hex {
        return Hex(h.r, h.s, h.q)
    }

    /**
     * Rotate hex to 180 degrees relative to (0, 0, 0)
     */
    fun rotateFlip(h: Hex): Hex {
        return Hex(-h.q, -h.r, -h.s)
    }

}