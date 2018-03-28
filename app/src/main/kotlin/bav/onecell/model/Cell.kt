package bav.onecell.model

import bav.onecell.model.hexes.Hex
import kotlin.math.abs

class Cell(val hexes: MutableSet<Hex> = mutableSetOf(), var center: Hex = Hex(0, 0, 0)) {

    fun clone() = Cell(hexes.toMutableSet())

    fun size(): Int = hexes.map { maxOf(maxOf(abs(it.q), abs(it.r)), abs(it.s)) }.max()?.let { it + 1 } ?: 0
}
