package bav.onecell.model.cell

import bav.onecell.model.battle.Bullet.Companion.OMNI_BULLET_TIMEOUT
import bav.onecell.model.cell.logic.Action
import bav.onecell.model.cell.logic.BattleFieldState
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import kotlin.math.PI
import kotlin.math.abs

// TODO: add cell visibility radius - cell may observe battlefield within limited radius from cell's center
class Cell(private val hexMath: HexMath,
           val data: Data = Data(),
           val battleData: BattleData = BattleData()) {

    enum class Direction {
        N, NE, SE, S, SW, NW;

        companion object {
            private val map = Direction.values().associateBy { it.ordinal }
            fun fromInt(type: Int): Direction = map[type] ?: N
        }
    }

    data class AnimationData(var rotation: Float = 0f,
                             var moveDirection: Int = 0, var movingFraction: Float = 0f,
                             var hexHashesToRemove: List<Pair<Int, Int>>? = null, var fadeFraction: Float = 0f)
    val animationData = AnimationData()

    data class BattleData(var cellIdInCurrentSnapshot: Int = 0,
                          var isAlive: Boolean = true,
                          var omniBulletTimeout: Int = OMNI_BULLET_TIMEOUT) {
        fun clone(): BattleData = BattleData(cellIdInCurrentSnapshot, isAlive, omniBulletTimeout)
    }

    companion object {
        const val TAG = "Cell"
    }

    private val outlineHexes: MutableSet<Hex> = mutableSetOf()
    private val hexNeighborsPool: MutableList<Hex> = mutableListOf()

    init {
        for (i in 0..5) hexNeighborsPool.add(Hex())
        updateOutlineHexes()
    }

    fun clear() {
        data.clear()
    }

    fun hasHexesInBucket(type: Hex.Type): Boolean = (data.hexBucket[type.ordinal] ?: 0) > 0

    fun clone(): Cell = Cell(hexMath, data.clone(), battleData.clone())

    fun size(): Int = data.hexes.values.map { maxOf(maxOf(abs(it.q), abs(it.r)), abs(it.s)) }.max()?.let { it + 1 } ?: 0

    fun addHex(hex: Hex) {
        data.hexes[Pair(hex.q, hex.r)] = hex.clone()
        updateOutlineHexes()
    }

    fun removeHex(hex: Hex) {
        data.hexes.remove(Pair(hex.q, hex.r))
        updateOutlineHexes()
    }

    fun contains(hex: Hex): Boolean = data.hexes.containsKey(Pair(hex.q, hex.r))

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
                Hex.Type.DEATH_RAY -> {
                    hex.power = Hex.Power.DEATH_RAY_SELF.value
                }
                Hex.Type.OMNI_BULLET -> {
                    hex.power = Hex.Power.OMNI_BULLET_SELF.value
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
        /// TODO: optimize it even more, don't allocate/clear hexes in a hull each time
        outlineHexes.clear()
        data.hexes.forEach { entry ->
            hexMath.hexNeighbors(entry.value, hexNeighborsPool)
            for (neighbor in hexNeighborsPool) {
                if (!data.hexes.values.contains(neighbor))
                    outlineHexes.add(neighbor.copy())
            }
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
        rotate(newDir, false)
    }

    fun rotateRight() {
        val newDir = when (data.direction) {
            Direction.NW -> Direction.N
            else -> Direction.fromInt(data.direction.ordinal + 1)
        }
        rotate(newDir, false)
    }

    /**
     * Simple formula has been provided by Danil Bogaevsky.
     */
    fun rotate(newDirection: Direction, updateDirection: Boolean = true) {
        var r = (data.direction.ordinal - newDirection.ordinal) % 6
        if (r < 0) r += 6
        when (r) {
            1 -> rotateHexesLeft()
            2 -> rotateHexesLeftTwice()
            3 -> rotateHexesFlip()
            4 -> rotateHexesRightTwice()
            5 -> rotateHexesRight()
            else -> Unit
        }
        if (updateDirection) data.direction = newDirection
    }

    private fun rotateHexesFlip() {
        val newHexes = mutableMapOf<Pair<Int, Int>, Hex>()
        data.hexes.forEach {
            val newHex = hexMath.rotateFlip(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.mapKey] = newHex
        }
        data.hexes = newHexes
    }

    private fun rotateHexesRight() {
        val newHexes = mutableMapOf<Pair<Int, Int>, Hex>()
        data.hexes.forEach {
            val newHex = hexMath.rotateRight(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.mapKey] = newHex
        }
        data.hexes = newHexes
    }

    private fun rotateHexesRightTwice() {
        val newHexes = mutableMapOf<Pair<Int, Int>, Hex>()
        data.hexes.forEach {
            val newHex = hexMath.rotateRightTwice(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.mapKey] = newHex
        }
        data.hexes = newHexes
    }

    private fun rotateHexesLeft() {
        val newHexes = mutableMapOf<Pair<Int, Int>, Hex>()
        data.hexes.forEach {
            val newHex = hexMath.rotateLeft(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.mapKey] = newHex
        }
        data.hexes = newHexes
    }

    private fun rotateHexesLeftTwice() {
        val newHexes = mutableMapOf<Pair<Int, Int>, Hex>()
        data.hexes.forEach {
            val newHex = hexMath.rotateLeftTwice(it.value).withType(it.value.type).withPower(it.value.power)
            newHexes[newHex.mapKey] = newHex
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

    /// TODO: optimize?
    fun getRotationAngle(direction: Int): Float {
        val newDirection = Cell.Direction.fromInt(direction)

        val zeroRotation = 0f
        val rightRotation = PI.toFloat() / 3f
        val leftRotation = -PI.toFloat() / 3f
        val twiceRightRotation = 2f * PI.toFloat() / 3f
        val twiceLeftRotation = 2f * -PI.toFloat() / 3f
        val flipRotation = /*if (Math.random() < 0.5) PI.toFloat() else */-PI.toFloat()

        var r = (data.direction.ordinal - newDirection.ordinal) % 6
        if (r < 0) r += 6

        return when (r) {
            1 -> leftRotation
            2 -> twiceLeftRotation
            3 -> flipRotation
            4 -> twiceRightRotation
            5 -> rightRotation
            else -> zeroRotation
        }
    }

    fun rotateHex(hex: Hex, oldDirection: Direction, newDirection: Direction): Hex {
        var r = (oldDirection.ordinal - newDirection.ordinal) % 6
        if (r < 0) r += 6
        return when (r) {
            1 -> hexMath.rotateLeft(hex)
            2 -> hexMath.rotateLeftTwice(hex)
            3 -> hexMath.rotateFlip(hex)
            4 -> hexMath.rotateRightTwice(hex)
            5 -> hexMath.rotateRight(hex)
            else -> hex
        }
    }
}
