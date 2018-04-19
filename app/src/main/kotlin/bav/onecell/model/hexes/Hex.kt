package bav.onecell.model.hexes

import com.google.gson.Gson

data class Hex(var q: Int = 0, var r: Int = 0, var s: Int = 0) {

    companion object {
        fun fromJson(json: String): Hex {
            return Gson().fromJson(json, Hex::class.java)
        }
    }

    enum class Type {
        LIFE, ENERGY, ATTACK, REMOVE
    }

    enum class Power(val value: Int) {
        LIFE_SELF(2), ENERGY_SELF(1), LIFE_TO_NEIGHBOR(1),
        ENERGY_TO_NEIGHBOR(2), ENERGY_TO_FAR_NEIGHBOR(1)
    }

    var type: Type = Type.REMOVE
    var power: Int = 0
    var receivedDamage: Int = 0

    init {
        if (q + r + s != 0) throw IllegalArgumentException("q + r + s should be equal to 0")
    }

    fun withType(type: Type): Hex {
        this.type = type
        return this
    }

    fun withPower(power: Int): Hex {
        this.power = power
        return this
    }

    fun clone() = Hex(q, r, s).withType(type).withPower(power)

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}