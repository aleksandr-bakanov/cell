package bav.onecell.common.storage

import androidx.room.TypeConverter
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.Data
import bav.onecell.model.cell.logic.Rule
import bav.onecell.model.hexes.Hex
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {

    companion object {
        val MAP_OF_HEXES_TYPE: Type = object : TypeToken<Map<Pair<Int, Int>, Hex>>(){}.type
        val LIST_OF_RULES_TYPE: Type = object : TypeToken<List<Rule>>(){}.type
        val MAP_OF_PAIR_INT_TYPE: Type = object : TypeToken<Map<Int, Int>>(){}.type
        val PAIR_OF_INT_TYPE: Type = object : TypeToken<Pair<Int, Int>>(){}.type
    }

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
        return direction?.ordinal
    }

    @TypeConverter
    fun intToCellDirection(value: Int?): Cell.Direction? {
        return value?.let { Cell.Direction.fromInt(it) }
    }

    @TypeConverter
    fun mapOfHexesToString(hexes: Map<Pair<Int, Int>, Hex>?): String? = hexes?.let {
        GsonBuilder().enableComplexMapKeySerialization().create().toJson(it)
    }

    @TypeConverter
    fun stringToMapOfHexes(str: String?): Map<Pair<Int, Int>, Hex>? = str?.let {
        GsonBuilder().enableComplexMapKeySerialization().create().fromJson(it, MAP_OF_HEXES_TYPE)
    }

    @TypeConverter
    fun listOfRulesToString(rules: List<Rule>?): String? = rules?.let { Gson().toJson(it) }

    @TypeConverter
    fun stringToListOfRules(str: String?): List<Rule>? = str?.let { Gson().fromJson(it, LIST_OF_RULES_TYPE) }

    @TypeConverter
    fun mapOfIntToString(map: Map<Int, Int>?): String? = map?.let { Gson().toJson(it) }

    @TypeConverter
    fun stringToMapOfInt(str: String?): Map<Int, Int>? = str?.let { Gson().fromJson(it, MAP_OF_PAIR_INT_TYPE) }

    @TypeConverter
    fun pairOfIntToString(pair: Pair<Int, Int>?): String? = pair?.let { Gson().toJson(it) }

    @TypeConverter
    fun stringToPairOfInt(str: String?): Pair<Int, Int>? = str?.let { Gson().fromJson(it, PAIR_OF_INT_TYPE) }
}
