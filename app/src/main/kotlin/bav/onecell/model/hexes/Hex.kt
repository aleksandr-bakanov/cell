package bav.onecell.model.hexes

import com.google.gson.Gson

data class Hex(var q /*X*/: Int = 0, var r /*Z*/: Int = 0, var s /*Y*/: Int = 0) {

    companion object {
        fun fromJson(json: String): Hex {
            return Gson().fromJson(json, Hex::class.java)
        }
    }

    enum class Type {
        LIFE, ENERGY, ATTACK, DEATH_RAY, OMNI_BULLET, REMOVE
    }

    enum class Power(val value: Int) {
        LIFE_SELF(2), ENERGY_SELF(1), LIFE_TO_NEIGHBOR(1),
        ENERGY_TO_NEIGHBOR(2), ENERGY_TO_FAR_NEIGHBOR(1), DEATH_RAY_SELF(1),
        OMNI_BULLET_SELF(1);
    }

    enum class TransformPrice(val value: Int) {
        LIFE_TO_ATTACK(3), LIFE_TO_ENERGY(5), LIFE_TO_DEATH_RAY(10), LIFE_TO_OMNI_BULLET(15)
    }

    var type: Type = Type.REMOVE
    @Transient var power: Int = 0
    @Transient var receivedDamage: Int = 0
    @Transient val mapKey = Pair(q, r)

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
