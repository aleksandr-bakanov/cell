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
        val map = mutableMapOf<Hex.MapKey, Hex>()
        hexes.forEach { map[it.mapKey] = it }
        val data = Data(id = 1, hexes = map, origin = origin, direction = direction)

        val json = data.toJson()
        /*val json = """
            {
                "id":1,
                "hexes":{
                    "(1, 2)":{
                        "type":"REMOVE","q":1,"r":2,"s":-3
                    },
                    "(0, 5)":{
                        "type":"REMOVE","q":0,"r":5,"s":-5
                    }
                },
                "origin":{"type":"REMOVE","q":1,"r":2,"s":-3},
                "direction":"SW",
                "rules":[],
                "name":"",
                "groupId":0,
                "viewDistance":3,
                "hexBucket":{"0":5,"2":5,"1":5,"3":5,"4":5}
            }
        """.trimIndent()*/

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
