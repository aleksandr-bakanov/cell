package bav.onecell.model

import bav.onecell.model.hexes.Hex

data class Cell(val hexes: MutableSet<Hex> = mutableSetOf()) {
}