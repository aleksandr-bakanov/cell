package bav.onecell.model.cell

import bav.onecell.model.cell.logic.Action
import bav.onecell.model.cell.logic.BattleFieldState
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlin.math.abs
import kotlin.math.max

// TODO: add cell visibility radius - cell may observe battlefield within limited radius from cell's center
class Cell(private val hexMath: HexMath,
           val data: Data = Data()) {

    enum class Direction {
        N, NE, SE, S, SW, NW;

        companion object {
            private val map = Direction.values().associateBy { it.ordinal }
            fun fromInt(type: Int): Direction = map[type] ?: N
        }
    }

    data class AnimationData(var rotation: Float = 0f,
                             var moveDirection: Int = 0, var movingFraction: Float = 0f,
                             var hexHashesToRemove: List<Int>? = null, var fadeFraction: Float = 0f)
    val animationData = AnimationData()

    companion object {
        const val TAG = "Cell"
    }

    private val outlineHexes: MutableSet<Hex> = mutableSetOf()

    init {
        updateOutlineHexes()
    }

    private val moneyProvider = BehaviorSubject.create<Int>()
    fun getMoneyProvider(): Observable<Int> = moneyProvider
    private fun setMoney(value: Int) {
        data.money = value
        moneyProvider.onNext(value)
    }
    fun addMoney(value: Int) = setMoney(data.money + value)
    fun removeMoney(value: Int) = setMoney(max(data.money - value, 0))

    fun hexTypeToPrice(type: Hex.Type): Int = when (type) {
        Hex.Type.LIFE -> Hex.Price.LIFE.value
        Hex.Type.ENERGY -> Hex.Price.ENERGY.value
        Hex.Type.ATTACK -> Hex.Price.ATTACK.value
        else -> 0
    }

    fun clone(): Cell = Cell(hexMath, data.clone())

    fun size(): Int = data.hexes.values.map { maxOf(maxOf(abs(it.q), abs(it.r)), abs(it.s)) }.max()?.let { it + 1 } ?: 0

    fun addHex(hex: Hex) {
        data.hexes[hex.hashCode()] = hex
        updateOutlineHexes()
    }

    fun removeHex(hex: Hex) {
        data.hexes.remove(hex.hashCode())
        updateOutlineHexes()
    }

    fun contains(hex: Hex): Boolean = data.hexes.containsKey(hex.hashCode())

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
            else -> Direction.fromInt(data.direction.ordinal - 1)
        }
        rotate(newDir)
    }

    fun rotateRight() {
        val newDir = when (data.direction) {
            Direction.NW -> Direction.N
            else -> Direction.fromInt(data.direction.ordinal + 1)
        }
        rotate(newDir)
    }

    // TODO: make it formula
    fun rotate(newDirection: Direction) {
        if (data.direction == newDirection) return
        else if (abs(data.direction.ordinal - newDirection.ordinal) == 3) rotateHexesFlip()
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

    /**
     * Applies cell's logic
     *
     * @param state Current state of battle from this cell point of view
     * @return Action to be performed
     */
    fun applyCellLogic(state: BattleFieldState): Action? {
        var action: Action? = null
        for (index in 0 until data.rules.size) {
            val rule = data.rules[index]
            if (rule.check(state)) {
                rule.action.perform(this)
                action = rule.action
                break
            }
        }
        return action
    }
}
