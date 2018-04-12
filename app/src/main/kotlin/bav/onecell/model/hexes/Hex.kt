package bav.onecell.model.hexes

data class Hex(val q: Int, val r: Int, val s: Int) {

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
}