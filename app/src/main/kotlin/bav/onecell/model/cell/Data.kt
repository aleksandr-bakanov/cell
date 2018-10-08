package bav.onecell.model.cell

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import bav.onecell.model.cell.logic.Rule
import bav.onecell.model.hexes.Hex
import com.google.gson.Gson

@Entity(tableName = "cellData")
data class Data(
        @PrimaryKey var id: Long = 0,
        // hexes - hexes contained in cell
        @ColumnInfo(name = "hexes") var hexes: MutableMap<Int, Hex> =
                mutableMapOf(Pair(Hex().hashCode(), Hex().withType(Hex.Type.LIFE))),
        // origin - an origin coordinates
        @ColumnInfo(name = "origin") var origin: Hex = Hex(0, 0, 0),
        // direction - direction of cell's look
        @ColumnInfo(name = "direction") var direction: Cell.Direction = Cell.Direction.N,
        // rules - set of rules according to which cell will act in battle
        @ColumnInfo(name = "rules") var rules: MutableList<Rule> = arrayListOf(),
        // name - cell's name
        @ColumnInfo(name = "name") var name: String = "",
        // money - can be spent to build up cell
        @ColumnInfo(name = "money") var money: Int = 10,
        // group id - cells with same group id are friends to each other
        @ColumnInfo(name = "groupId") var groupId: Int = 0) {

    companion object {
        fun fromJson(json: String): Data {
            return Gson().fromJson(json, Data::class.java)
        }
    }

    fun clone(): Data {
        val data = Data(origin = origin, direction = direction, id = id, groupId = groupId)
        data.hexes.clear()
        hexes.forEach { entry -> data.hexes[entry.key] = entry.value.clone() }
        rules.forEach { data.rules.add(it) }
        return data
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }
}
