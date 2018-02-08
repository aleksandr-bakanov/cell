package bav.onecell.model.hexes

import kotlin.math.sqrt

data class Orientation(val f0: Double, val f1: Double, val f2: Double, val f3: Double,
                       val b0: Double, val b1: Double, val b2: Double, val b3: Double,
                       val startAngle: Double) {

    companion object {
        val LAYOUT_POINTY: Orientation = Orientation(
            sqrt(3.0), sqrt(3.0) / 2.0, 0.0, 3.0 / 2.0,
            sqrt(3.0) / 3.0, -1.0 / 3.0, 0.0, 2.0 / 3.0,
            0.5)
        val LAYOUT_FLAT: Orientation = Orientation(
            3.0 / 2.0, 0.0, sqrt(3.0) / 2.0, sqrt(3.0),
            2.0 / 3.0, 0.0, -1.0 / 3.0, sqrt(3.0) / 3.0,
            0.0)
    }

}