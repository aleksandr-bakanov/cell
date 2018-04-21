package bav.onecell.common.storage

import android.arch.persistence.room.TypeConverter
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.Data
import bav.onecell.model.hexes.Hex
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {
    private val mapOfHexesType: Type = object : TypeToken<Map<Int, Hex>>(){}.type

    @TypeConverter
    fun cellDataToString(data: Data?): String? {
        return data?.toJson()
    }

    @TypeConverter
    fun stringToCellData(str: String?): Data? {
        return str?.let { Data.fromJson(it) }
    }

    @TypeConverter
    fun hexToString(hex: Hex?): String? {
        return hex?.toJson()
    }

    @TypeConverter
    fun stringToHex(str: String?): Hex? {
        return str?.let { Hex.fromJson(it) }
    }

    @TypeConverter
    fun cellDirectionToInt(direction: Cell.Direction?): Int? {
        return direction?.value
    }

    @TypeConverter
    fun intToCellDirection(value: Int?): Cell.Direction? {
        return value?.let { Cell.Direction.fromInt(it) }
    }

    @TypeConverter
    fun mapOfHexesToString(hexes: Map<Int, Hex>?): String? {
        return hexes?.let {
            Gson().toJson(it)
        }
    }

    @TypeConverter
    fun stringToMapOfHexes(str: String?): Map<Int, Hex>? {
        return str?.let {
            Gson().fromJson(it, mapOfHexesType)
        }
    }
}
