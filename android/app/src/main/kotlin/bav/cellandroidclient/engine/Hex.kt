package bav.cellandroidclient.engine

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin

data class Hex(val q: Int, val r: Int, val s: Int) {

  companion object {
      private val HEX_DIRECTIONS: Array<Hex> = arrayOf(
          Hex(1, 0, -1), Hex(1, -1, 0), Hex(0, -1, 1),
          Hex(-1, 0, 1), Hex(-1, 1, 0), Hex(0, 1, -1)
      )

      fun hexDirection(direction: Int /* 0 to 5 */): Hex {
          assert(direction in 0..5)
          return HEX_DIRECTIONS[direction]
      }

      fun hexNeighbor(hex: Hex, direction: Int): Hex {
          return hexAdd(hex, hexDirection(direction))
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
          return hexLength(hexSubstract(a, b))
      }

      fun hexToPixel(layout: Layout, h: Hex): Point {
          val m: Orientation = layout.orientation
          val x: Double = (m.f0 * h.q + m.f1 * h.r) * layout.size.x
          val y: Double = (m.f2 * h.q + m.f3 * h.r) * layout.size.y
          return Point(x + layout.origin.x, y + layout.origin.y)
      }

      fun pixelToHex(layout: Layout, p: Point): FractionalHex {
          val m: Orientation = layout.orientation
          val pt: Point = Point((p.x - layout.origin.x) / layout.size.x,
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

      fun poligonCorners(layout: Layout, h: Hex): Array<Point> {
          val corners: Array<Point> = emptyArray()
          val center: Point = hexToPixel(layout, h)
          for (i in 0..5) {
              val offset: Point = hexCornerOffset(layout, i)
              corners[i] = Point(center.x + offset.x, center.y + offset.y)
          }
          return corners
      }
  }

  init {
      assert(q + r + s == 0)
  }

}