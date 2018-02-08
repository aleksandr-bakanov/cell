package bav.onecell.model

import bav.onecell.model.hexes.Hex

data class Cell(private val hexes: MutableSet<Hex> = mutableSetOf()) {
}