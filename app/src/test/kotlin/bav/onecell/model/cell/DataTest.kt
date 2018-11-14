package bav.onecell.model.cell

import bav.onecell.model.hexes.Hex
import org.junit.Assert.assertEquals
import org.junit.Test

class DataTest {

    @Test
    fun serializationShouldWorkFine() {
        val origin = Hex()
        val direction = Cell.Direction.SW
        val hexes = arrayOf(Hex().withType(Hex.Type.LIFE))
        val map = mutableMapOf<Pair<Int, Int>, Hex>()
        hexes.forEach { map[it.mapKey] = it }
        val data = Data(id = 1, hexes = map, origin = origin, direction = direction)

        val json = data.toJson()
        System.out.println("json = $json")
        val clone = Data.fromJson(json)

        assertEquals(origin, clone.origin)
        assertEquals(direction, clone.direction)
        assertEquals(hexes.size, clone.hexes.size)
        for (h in data.hexes.values) {
            assertEquals(h, clone.hexes[h.mapKey])
        }
    }
}
