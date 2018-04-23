package bav.onecell.model.cell

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import bav.onecell.model.hexes.Hex
import com.google.gson.Gson

@Entity(tableName = "cellData")
data class Data(
        @PrimaryKey(autoGenerate = true) var id: Long? = null,
        @ColumnInfo(name = "hexes") var hexes: MutableMap<Int, Hex> = mutableMapOf(),
        @ColumnInfo(name = "origin") var origin: Hex = Hex(0, 0, 0),
        @ColumnInfo(name = "direction") var direction: Cell.Direction = Cell.Direction.N) {

    companion object {
        fun fromJson(json: String): Data {
            return Gson().fromJson(json, Data::class.java)
        }
    }

    fun clone(): Data {
        val data = Data(origin = origin, direction = direction)
        hexes.forEach { (k, v) -> data.hexes[k] = v.clone() }
        return data
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }
}
