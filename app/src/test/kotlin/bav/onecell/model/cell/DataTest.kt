package bav.onecell.model.cell

import bav.onecell.model.hexes.Hex
import org.junit.Assert.assertEquals
import org.junit.Test

class DataTest {

    @Test
    fun serializationShouldWorkFine() {
        val origin = Hex(1, 2, -3)
        val direction = Cell.Direction.SW
        val hexes = arrayOf(Hex(1, 2, -3), Hex(0, 5, -5))
        val map = mutableMapOf<Int, Hex>()
        hexes.forEach { map[it.hashCode()] = it }
        val data = Data(id = 1, hexes = map, origin = origin, direction = direction)

        val json = data.toJson()

        val clone = Data.fromJson(json)

        assertEquals(origin, clone.origin)
        assertEquals(direction, clone.direction)
        assertEquals(hexes.size, clone.hexes.size)
        for (h in data.hexes.values) {
            assertEquals(h, clone.hexes[h.hashCode()])
        }
    }
}
