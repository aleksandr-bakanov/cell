package bav.onecell.model.cell

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import bav.onecell.common.storage.Converters
import bav.onecell.model.cell.logic.Rule
import bav.onecell.model.hexes.Hex
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type

@Entity(tableName = "cellData")
data class Data(
        @PrimaryKey var id: Long = 0,
        // hexes - hexes contained in cell, keys of the map are pairs of (q, r) coordinates
        @ColumnInfo(name = "hexes") var hexes: MutableMap<Pair<Int, Int>, Hex> =
                mutableMapOf(Pair(0, 0) to Hex().withType(Hex.Type.LIFE)),
        // origin - an origin coordinates
        @ColumnInfo(name = "origin") var origin: Hex = Hex(),
        // direction - direction of cell's look
        @ColumnInfo(name = "direction") var direction: Cell.Direction = Cell.Direction.N,
        // rules - set of rules according to which cell will act in battle
        @ColumnInfo(name = "rules") var rules: MutableList<Rule> = arrayListOf(),
        // name - cell's name
        @ColumnInfo(name = "name") var name: String = "",
        // group id - cells with the same group id are friends to each other
        @ColumnInfo(name = "groupId") var groupId: Int = 0,
        // field of view - distance of view through the fog
        @ColumnInfo(name = "viewDistance") var viewDistance: Int = 3,
        // hex bucket - hexes to be used to build up cell from. Map of Hex.Type.ordinal -> count.
        @ColumnInfo(name = "hexBucket") var hexBucket: MutableMap<Int, Int> =
                mutableMapOf(Hex.Type.LIFE.ordinal to 5, Hex.Type.ATTACK.ordinal to 5,
                             Hex.Type.ENERGY.ordinal to 5, Hex.Type.DEATH_RAY.ordinal to 5,
                             Hex.Type.OMNI_BULLET.ordinal to 5)) {

    fun clear() {
        hexes.clear()
        rules.clear()
        hexBucket.clear()
    }

    companion object {
        private val pairIntAdapter = object : TypeAdapter<Pair<Int, Int>>(),
                                              InstanceCreator<Pair<Int, Int>>,
                                              JsonSerializer<Pair<Int, Int>>,
                                              JsonDeserializer<Pair<Int, Int>> {
            // TypeAdapter
            override fun read(reader: JsonReader?): Pair<Int, Int>? {
                if (reader?.peek() == JsonToken.NULL) {
                    reader.nextNull()
                    return null
                }
                return reader?.nextString()?.let { xy ->
                    // Omit brackets in (x,y)
                    val parts = xy.substring(1, xy.length - 1).split(",")
                    Pair(parts[0].toInt(), parts[1].toInt())
                }
            }
            override fun write(writer: JsonWriter?, value: Pair<Int, Int>?) {
                if (value == null) {
                    writer?.nullValue()
                }
                else {
                    writer?.value("(${value.first},${value.second})")
                }
            }

            // InstanceCreator
            override fun createInstance(type: Type?): Pair<Int, Int> = Pair(0, 0)

            // JsonSerializer
            override fun serialize(src: Pair<Int, Int>?, typeOfSrc: Type?,
                                   context: JsonSerializationContext?): JsonElement {
                return JsonPrimitive("(${src?.first},${src?.second})")
            }

            // JsonDeserializer
            override fun deserialize(json: JsonElement?, typeOfT: Type?,
                                     context: JsonDeserializationContext?): Pair<Int, Int> {
                return json?.asString.let { xy ->
                    // Omit brackets in (x,y)
                    val parts = xy?.substring(1, xy?.length - 1)?.split(",")
                    Pair(parts?.get(0)?.toInt() ?: 0, parts?.get(1)?.toInt() ?: 0)
                }
            }
        }

        fun fromJson(json: String): Data = GsonBuilder()
                /// TODO: implement pretty output of pair of ints for Gson later
                //.registerTypeAdapter(Converters.PAIR_OF_INT_TYPE, pairIntAdapter)
                .enableComplexMapKeySerialization()
                .create()
                .fromJson(json, Data::class.java)
    }

    fun clone(): Data {
        val data = Data(origin = origin, direction = direction, id = id, groupId = groupId, viewDistance = viewDistance,
                        name = name)
        data.hexes.clear()
        hexes.forEach { entry -> data.hexes[entry.key] = entry.value.clone() }
        rules.forEach { data.rules.add(it) }
        data.hexBucket.putAll(hexBucket)
        return data
    }

    fun toJson(): String = GsonBuilder()
            //.registerTypeAdapter(Converters.PAIR_OF_INT_TYPE, pairIntAdapter)
            .enableComplexMapKeySerialization()
            .create()
            .toJson(this)
}
