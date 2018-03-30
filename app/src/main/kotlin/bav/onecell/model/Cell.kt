package bav.onecell.model

import bav.onecell.model.hexes.Hex
import kotlin.math.abs

class Cell(var hexes: MutableSet<Hex> = mutableSetOf(),
           var origin: Hex = Hex(0, 0, 0),
           var direction: Direction = Direction.N) {

    enum class Direction(value: Int) {
        N(0), NE(1), SE(2), S(3), SW(4), NW(5);

        companion object {
            private val map = Direction.values().associateBy { it.ordinal }
            fun fromInt(type: Int): Direction = map[type] ?: N
        }
    }

    fun clone() = Cell(hexes.toMutableSet(), origin, direction)

    fun size(): Int = hexes.map { maxOf(maxOf(abs(it.q), abs(it.r)), abs(it.s)) }.max()?.let { it + 1 } ?: 0

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
        val newHexes = mutableSetOf<Hex>()
        hexes.forEach {
            newHexes.add(Hex.rotateFlip(it).withType(it.type))
        }
        hexes = newHexes
    }

    private fun rotateHexesRight() {
        val newHexes = mutableSetOf<Hex>()
        hexes.forEach {
            newHexes.add(Hex.rotateRight(it).withType(it.type))
        }
        hexes = newHexes
    }

    private fun rotateHexesRightTwice() {
        val newHexes = mutableSetOf<Hex>()
        hexes.forEach {
            newHexes.add(Hex.rotateRightTwice(it).withType(it.type))
        }
        hexes = newHexes
    }

    private fun rotateHexesLeft() {
        val newHexes = mutableSetOf<Hex>()
        hexes.forEach {
            newHexes.add(Hex.rotateLeft(it).withType(it.type))
        }
        hexes = newHexes
    }

    private fun rotateHexesLeftTwice() {
        val newHexes = mutableSetOf<Hex>()
        hexes.forEach {
            newHexes.add(Hex.rotateLeftTwice(it).withType(it.type))
        }
        hexes = newHexes
    }
}
