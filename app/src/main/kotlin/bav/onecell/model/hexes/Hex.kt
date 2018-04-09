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

    enum class Power(val value: Int) {
        LIFE_SELF(2), ENERGY_SELF(1), LIFE_TO_NEIGHBOR(1),
        ENERGY_TO_NEIGHBOR(2), ENERGY_TO_FAR_NEIGHBOR(1)
    }

    companion object {

        val ZERO_HEX = Hex(0, 0, 0)

        private val HEX_DIRECTIONS: Array<Hex> = arrayOf(
                Hex(1, -1, 0), // 0
                Hex(1, 0, -1), // 1
                Hex(0, 1, -1), // 2
                Hex(-1, 1, 0), // 3
                Hex(-1, 0, 1), // 4
                Hex(0, -1, 1)  // 5
        )

        fun hexDirection(direction: Int /* 0 to 5 */): Hex {
            assert(direction in 0..5)
            return HEX_DIRECTIONS[direction]
        }

        fun getNeighborsWithinRadius(origin: Hex, radius: Int): Set<Hex> {
            val hexes = mutableSetOf<Hex>()
            for (q in -radius..radius) {
                val r1 = Math.max(-radius, -q - radius)
                val r2 = Math.min(radius, -q + radius)
                for (r in r1..r2) {
                    hexes.add(hexAdd(origin, Hex(q, r, -q - r)))
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
            return if (angle >= (-PI * 5 / 6) && angle < (-PI / 2)) 5
            else if (angle >= (-PI / 2) && angle < (-PI / 6)) 0
            else if (angle >= (-PI / 6) && angle < (PI / 6)) 1
            else if (angle >= (PI / 6) && angle < (PI / 2)) 2
            else if (angle >= (PI / 2) && angle < (PI * 5 / 6)) 3
            else 4
        }

        fun hexNeighbor(hex: Hex, direction: Int): Hex {
            return hexAdd(hex, hexDirection(direction))
        }

        //  5    0
        //    /\
        // 4 |  | 1
        //    \/
        //  3    2
        fun hexNeighbors(hex: Hex): Collection<Hex> {
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

    var type: Type = Type.REMOVE
    var power: Int = 0
    var receivedDamage: Int = 0

    init {
        assert(q + r + s == 0)
    }

    fun withType(type: Type): Hex {
        this.type = type
        return this
    }

    fun withPower(power: Int): Hex {
        this.power = power
        return this
    }

    fun clone() = Hex(q, r, s).withType(type).withPower(power)

}