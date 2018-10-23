package bav.onecell.model

import com.google.gson.Gson

data class InitialBattleParams(
        val cellIndexes: MutableList<Int> = mutableListOf(),
        val isFog: Boolean = false) {

    companion object {
        fun toJson(params: InitialBattleParams): String = Gson().toJson(params)
        fun fromJson(json: String): InitialBattleParams = Gson().fromJson(json, InitialBattleParams::class.java)
    }
}
