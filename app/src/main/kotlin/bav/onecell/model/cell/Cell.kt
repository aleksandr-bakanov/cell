package bav.onecell.model.cell

import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import kotlin.math.abs

// TODO: add cell visibility radius - cell may observe battlefield within limited radius from cell's center
class Cell(private val hexMath: HexMath,
           val data: Data = Data()) {

    enum class Direction(val value: Int) {
        N(0), NE(1), SE(2), S(3), SW(4), NW(5);
        companion object {
            private val map = Direction.values().associateBy { it.value }
            fun fromInt(type: Int): Direction = map[type] ?: N
        }
    }

    companion object {
        const val TAG = "Cell"
    }

    private val outlineHexes: MutableSet<Hex> = mutableSetOf()

    fun clone(): Cell = Cell(hexMath, data.clone())

    fun size(): Int = data.hexes.values.map { maxOf(maxOf(abs(it.q), abs(it.r)), abs(it.s)) }.max()?.let { it + 1 } ?: 0

    fun addHex(hex: Hex) {
        data.hexes[hex.hashCode()] = hex
    }

    fun removeHex(hex: Hex) {
        data.hexes.remove(hex.hashCode())
    }

    fun contains(hex: Hex): Boolean = data.hexes[hex.hashCode()] != null

    fun evaluateCellHexesPower() {
        data.hexes.values.forEach { hex ->
            hex.power = 0
            when (hex.type) {
                Hex.Type.LIFE -> {
                    hex.power = Hex.Power.LIFE_SELF.value
                }
                Hex.Type.ENERGY -> {
                    hex.power = Hex.Power.ENERGY_SELF.value
                }
                Hex.Type.ATTACK -> {
                    data.hexes.values.intersect(hexMath.getNeighborsWithinRadius(hex, 2)).forEach { neighbor ->
                        val distance = hexMath.distance(hex, neighbor)
                        when (neighbor.type) {
                            Hex.Type.LIFE -> {
                                if (distance == 1) hex.power += Hex.Power.LIFE_TO_NEIGHBOR.value
                            }
                            Hex.Type.ENERGY -> {
                                hex.power += when (distance) {
                                    1 -> Hex.Power.ENERGY_TO_NEIGHBOR.value
                                    2 -> Hex.Power.ENERGY_TO_FAR_NEIGHBOR.value
                                    else -> 0
                                }
                            }
                            else -> Unit
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    /**
     * Returns outline border of cell in local coordinates
     * TODO: make it faster, probably using some convex hull algorithm
     *
     * @return Set of hexes which forms outline of cell
     */
    fun getOutlineHexes(): Collection<Hex> = outlineHexes

    fun updateOutlineHexes() {
        outlineHexes.clear()
        data.hexes.forEach { entry ->
            outlineHexes.addAll(hexMath.hexNeighbors(entry.value).subtract(data.hexes.values))
        }
    }

    /**
     * Returns hexes which forms border of cell
     *
     * @return Set of hexes which forms border of cell
     */
    fun getBorderHexes(): Collection<Hex> {
        // TODO: implement this
        return setOf()
    }

    fun rotateLeft() {
        val newDir = when (data.direction) {
            Direction.N -> Direction.NW
            else -> Direction.fromInt(data.direction.value - 1)
        }
        rotate(newDir)
    }

    fun rotateRight() {
        val newDir = when (data.direction) {
            Direction.NW -> Direction.N
            else -> Direction.fromInt(data.direction.value + 1)
        }
        rotate(newDir)
    }

    // TODO: make it formula
    fun rotate(newDirection: Direction) {
        if (data.direction == newDirection) return
        else if (abs(data.direction.value - newDirection.value) == 3) rotateHexesFlip()
        else {
            when (data.direction) {
                Direction.N -> {
                    when (newDirection) {
                        Direction.NE -> rotateHexesRight()
                        Direction.SE -> rotateHexesRightTwice()
                        Direction.NW -> rotateHexesLeft()
                        Direction.SW -> rotateHexesLeftTwice()
                        else -> Unit
                    }
                }
                Direction.NE -> {
                    when (newDirection) {
                        Direction.SE -> rotateHexesRight()
                        Direction.S -> rotateHexesRightTwice()
                        Direction.N -> rotateHexesLeft()
                        Direction.NW -> rotateHexesLeftTwice()
                        else -> Unit
                    }
                }
                Direction.SE -> {
                    when (newDirection) {
                        Direction.S -> rotateHexesRight()
                        Direction.SW -> rotateHexesRightTwice()
                        Direction.NE -> rotateHexesLeft()
                        Direction.N -> rotateHexesLeftTwice()
                        else -> Unit
                    }
                }
                Direction.S -> {
                    when (newDirection) {
                        Direction.SW -> rotateHexesRight()
                        Direction.NW -> rotateHexesRightTwice()
                        Direction.SE -> rotateHexesLeft()
                        Direction.NE -> rotateHexesLeftTwice()
                        else -> Unit
                    }
                }
                Direction.SW -> {
                    when (newDirection) {
                        Direction.NW -> rotateHexesRight()
                        Direction.N -> rotateHexesRightTwice()
                        Direction.S -> rotateHexesLeft()
                        Direction.SE -> rotateHexesLeftTwice()
                        else -> Unit
                    }
                }
                Direction.NW -> {
                    when (newDirection) {
                        Direction.N -> rotateHexesRight()
                        Direction.NE -> rotateHexesRightTwice()
                        Direction.SW -> rotateHexesLeft()
                        Direction.S -> rotateHexesLeftTwice()
                        else -> Unit
                    }
                }
            }
        }
        data.direction = newDirection
    }

    private fun rotateHexesFlip() {
        val newHexes = mutableMapOf<Int, Hex>()
        data.hexes.forEach {
            val newHex = hexMath.rotateFlip(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.hashCode()] = newHex
        }
        data.hexes = newHexes
    }

    private fun rotateHexesRight() {
        val newHexes = mutableMapOf<Int, Hex>()
        data.hexes.forEach {
            val newHex = hexMath.rotateRight(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.hashCode()] = newHex
        }
        data.hexes = newHexes
    }

    private fun rotateHexesRightTwice() {
        val newHexes = mutableMapOf<Int, Hex>()
        data.hexes.forEach {
            val newHex = hexMath.rotateRightTwice(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.hashCode()] = newHex
        }
        data.hexes = newHexes
    }

    private fun rotateHexesLeft() {
        val newHexes = mutableMapOf<Int, Hex>()
        data.hexes.forEach {
            val newHex = hexMath.rotateLeft(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.hashCode()] = newHex
        }
        data.hexes = newHexes
    }

    private fun rotateHexesLeftTwice() {
        val newHexes = mutableMapOf<Int, Hex>()
        data.hexes.forEach {
            val newHex = hexMath.rotateLeftTwice(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.hashCode()] = newHex
        }
        data.hexes = newHexes
    }
}
