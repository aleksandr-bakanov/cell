package bav.onecell.model

import com.google.gson.Gson

data class InitialBattleParams(
        val cellIndexes: MutableList<Int> = mutableListOf(),
        val isFog: Boolean = false,
        val origins: MutableMap<String, HexCoord> = mutableMapOf()) {

    companion object {
        fun toJson(params: InitialBattleParams): String = Gson().toJson(params)
        fun fromJson(json: String): InitialBattleParams = Gson().fromJson(json, InitialBattleParams::class.java)
    }

    data class HexCoord(val q: Int = 0, val r: Int = 0, val s: Int = 0)
}
