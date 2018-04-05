package bav.onecell.model

import android.util.Log
import bav.onecell.model.hexes.Hex
import kotlin.math.abs

class Cell(var hexes: MutableMap<Int, Hex> = mutableMapOf(),
           var origin: Hex = Hex(0, 0, 0),
           var direction: Direction = Direction.N) {

    enum class Direction(value: Int) {
        N(0), NE(1), SE(2), S(3), SW(4), NW(5);

        companion object {
            private val map = Direction.values().associateBy { it.ordinal }
            fun fromInt(type: Int): Direction = map[type] ?: N
        }
    }

    companion object {
        const val TAG = "Cell"
    }

    fun clone(): Cell {
        val cell = Cell(origin = origin, direction = direction)
        hexes.forEach { k, v -> cell.hexes[k] = v.clone() }
        return cell
    }

    fun size(): Int = hexes.values.map { maxOf(maxOf(abs(it.q), abs(it.r)), abs(it.s)) }.max()?.let { it + 1 } ?: 0

    fun addHex(hex: Hex) {
        hexes[hex.hashCode()] = hex
    }

    fun removeHex(hex: Hex) {
        hexes.remove(hex.hashCode())
    }

    fun evaluateCellHexesPower() {
        hexes.values.forEach { hex ->
            hex.power = 0
            when (hex.type) {
                Hex.Type.LIFE -> {
                    hex.power = Hex.Power.LIFE_SELF.value
                }
                Hex.Type.ENERGY -> {
                    hex.power = Hex.Power.ENERGY_SELF.value
                }
                Hex.Type.ATTACK -> {
                    hexes.values.intersect(Hex.getNeighborsWithinRadius(hex, 2)).forEach { neighbor ->
                        val distance = Hex.hexDistance(hex, neighbor)
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

    fun rotateLeft() {
        val newDir = when (direction) {
            Direction.N -> Direction.NW
            else -> Direction.fromInt(direction.ordinal - 1)
        }
        rotate(newDir)
    }

    fun rotateRight() {
        val newDir = when (direction) {
            Direction.NW -> Direction.N
            else -> Direction.fromInt(direction.ordinal + 1)
        }
        rotate(newDir)
    }

    // TODO: make it formula
    fun rotate(newDirection: Direction) {
        if (direction == newDirection) return
        else if (abs(direction.ordinal - newDirection.ordinal) == 3) rotateHexesFlip()
        else {
            when (direction) {
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
        direction = newDirection
    }

    private fun rotateHexesFlip() {
        val newHexes = mutableMapOf<Int, Hex>()
        hexes.forEach {
            val newHex = Hex.rotateFlip(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.hashCode()] = newHex
        }
        hexes = newHexes
    }

    private fun rotateHexesRight() {
        val newHexes = mutableMapOf<Int, Hex>()
        hexes.forEach {
            val newHex = Hex.rotateRight(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.hashCode()] = newHex
        }
        hexes = newHexes
    }

    private fun rotateHexesRightTwice() {
        val newHexes = mutableMapOf<Int, Hex>()
        hexes.forEach {
            val newHex = Hex.rotateRightTwice(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.hashCode()] = newHex
        }
        hexes = newHexes
    }

    private fun rotateHexesLeft() {
        val newHexes = mutableMapOf<Int, Hex>()
        hexes.forEach {
            val newHex = Hex.rotateLeft(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.hashCode()] = newHex
        }
        hexes = newHexes
    }

    private fun rotateHexesLeftTwice() {
        val newHexes = mutableMapOf<Int, Hex>()
        hexes.forEach {
            val newHex = Hex.rotateLeftTwice(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.hashCode()] = newHex
        }
        hexes = newHexes
    }
}
