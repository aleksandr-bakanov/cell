package bav.onecell.battle

import android.graphics.Path
import android.util.Log
import bav.onecell.common.Consts
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.BattleFieldSnapshot
import bav.onecell.model.BattleInfo
import bav.onecell.model.battle.FrameGraphics
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Action
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import bav.onecell.model.hexes.Layout
import bav.onecell.model.hexes.Point

class BattleGraphics(
        private val drawUtils: DrawUtils,
        private val hexMath: HexMath) : Battle.FramesFactory {

    private val observableAreaHexPool: MutableList<Hex> = mutableListOf()
    private val observableAreaNeighborsPool: MutableList<Hex> = mutableListOf()

    init {
        for (i in 0..5) observableAreaNeighborsPool.add(Hex())
    }

    private val frameState: FrameState = FrameState()
    override fun generateFrameGraphics(battleInfo: BattleInfo, timestamp: Long, out: FrameGraphics) {
        out.reset()
        getFrameState(battleInfo.snapshots, timestamp, frameState)
        val snapshot = battleInfo.snapshots[frameState.snapshotIndex]

        //-------------------------------------------------------------
        // Cells data correction
        // Actions
        snapshot.cells.forEachIndexed { index, cell ->
            // Reset actions data
            cell.animationData.rotation = 0f
            if (index >= 0 && index < snapshot.cellsActions.size) {
                snapshot.cellsActions[index]?.let { action ->
                    when (action.act) {
                        Action.Act.CHANGE_DIRECTION -> {
                            val angle = cell.getRotationAngle(action.value)
                            cell.animationData.rotation = if (angle == 0f) 0f else {
                                angle * /*frameState.actionFraction*/ frameState.movingFraction
                            }
                        }
                    }
                }
            }
        }

        // Moving
        snapshot.cells.forEachIndexed { index, cell ->
            if (index >= 0 && index < snapshot.movingDirections.size) {
                // Save move direction
                cell.animationData.moveDirection = snapshot.movingDirections[index]
                // Clear cell moving fraction
                cell.animationData.movingFraction = frameState.movingFraction
            }
        }
        snapshot.bullets.forEach { bullet ->
            bullet.movingFraction = frameState.movingFraction
        }

        // Hex removal
        snapshot.cells.forEachIndexed { index, cell ->
            if (index >= 0 && index < snapshot.hexesToRemove.size) {
                cell.animationData.hexHashesToRemove = snapshot.hexesToRemove[index]
                cell.animationData.fadeFraction = frameState.hexRemovalFraction
            }
        }

        //-------------------------------------------------------------
        // Generating points
        // Living cells
        if (snapshot.cells.isNotEmpty()) {
            snapshot.cells.forEach { cell -> drawUtils.getCellGraphicalRepresentation(cell, out.getLivingCell()) }
        }

        // Corpse cells
        if (snapshot.corpses.isNotEmpty()) {
            snapshot.corpses.forEach { corpse -> drawUtils.getCellGraphicalRepresentation(corpse, out.getCorpse()) }
        }

        // Death rays
        if (snapshot.deathRays.isNotEmpty()) {
            snapshot.deathRays.forEach { ray ->
                hexMath.hexToPixel(Layout.UNIT, ray.first, out.getDeathRayPoint())
                hexMath.hexToPixel(Layout.UNIT, ray.second, out.getDeathRayPoint())
            }
            out.deathRaysAlpha = if (frameState.deathRayFraction < 0.5) {
                (frameState.deathRayFraction * 2f * 255f).toInt()
            } else {
                ((1f - frameState.deathRayFraction) * 2f * 255f).toInt()
            }
        }

        // Bullets
        if (snapshot.bullets.isNotEmpty()) {
            snapshot.bullets.forEach { bullet -> drawUtils.getBulletPath(bullet, out.getBullet(), Layout.UNIT) }
        }

        // Field of view
        val battleDuration = battleInfo.snapshots.sumBy { it.duration() }.toLong()
        val isFog = battleInfo.isFog
        val isBattleWon = battleInfo.winnerGroupId == Consts.HERO_GROUP_ID
        // In case last frame and battle is won fog doesn't show
        if (isFog) {
            if (timestamp != battleDuration) {
                getObservableAreaPath(snapshot.cells, out.fieldOfView)
            }
            else if (!isBattleWon) {
                out.fullFog = true
            }
        }
    }

    /// TODO: optimize area definition (in terms of allocated memory)
    private val origin: Point = Point()
    private val cellOrigin = Point()
    private val vHex = Hex()

    private fun getObservableAreaPath(cells: List<Cell>, out: Path) {
        out.reset()
        // Count of hexes in current observable area
        var currentPoolIndex = 0
        // Indices of layer's start and end in pool
        var indexOfCellStart = 0
        var indexOfLayerStart = 0
        var indexOfLayerEnd = 0
        var currentLayerEnd = 0
        // Get area observed by cells with group id = 0 only, i.e. main heroes
        cells.forEach { cell ->
            if (cell.data.groupId == Consts.MAIN_CHARACTERS_GROUP_ID) {
                hexMath.hexToPixel(Layout.UNIT, cell.data.origin, cellOrigin)

                indexOfCellStart = currentPoolIndex

                // Hexes of cell
                indexOfLayerStart = currentPoolIndex
                cell.data.hexes.values.forEach { hex ->
                    addToObservableAreaPool(currentPoolIndex, hex, hexMath.ZERO_HEX)
                    currentPoolIndex++
                }
                indexOfLayerEnd = currentPoolIndex

                // Layers of view field
                for (layerNumber in 0 until cell.data.viewDistance) {
                    currentLayerEnd = indexOfLayerEnd
                    for (i in indexOfLayerStart until currentLayerEnd) {
                        val hex = observableAreaHexPool[i]
                        hexMath.hexNeighbors(hex, observableAreaNeighborsPool)
                        neighborsLoop@ for (nIndex in 0..5) {
                            for (aIndex in indexOfCellStart until currentPoolIndex) {
                                if (observableAreaHexPool[aIndex] == observableAreaNeighborsPool[nIndex]) continue@neighborsLoop
                            }
                            addToObservableAreaPool(currentPoolIndex, observableAreaNeighborsPool[nIndex], hexMath.ZERO_HEX)
                            currentPoolIndex++
                            indexOfLayerEnd++
                        }
                    }
                    indexOfLayerStart = currentLayerEnd
                }

                for (vIndex in indexOfCellStart until currentPoolIndex) {
                    hexMath.add(cell.data.origin, observableAreaHexPool[vIndex], vHex)
                    hexMath.hexToPixel(Layout.UNIT, vHex, origin)
                    drawUtils.rotatePoint(origin, cellOrigin, cell.animationData.rotation)
                    drawUtils.offsetPoint(origin, cell.animationData.moveDirection, cell.animationData.movingFraction, Layout.UNIT)
                    out.addCircle(origin.x.toFloat(), origin.y.toFloat(), Layout.UNIT.size.x.toFloat(), Path.Direction.CW)
                }
            }
        }
        out.close()
    }

    private fun addToObservableAreaPool(currentPoolIndex: Int, hex: Hex, origin: Hex) {
        if (observableAreaHexPool.size <= currentPoolIndex) {
            observableAreaHexPool.add(hexMath.add(hex, origin))
        }
        else {
            hexMath.add(hex, origin, observableAreaHexPool[currentPoolIndex])
        }
    }

    private fun getFrameState(snapshots: List<BattleFieldSnapshot>, timestamp: Long, /*out*/ state: FrameState) {
        var acc = 0
        var snapshotIndex = -1

        for (i in 0 until snapshots.size) {
            snapshotIndex++
            val snapshot = snapshots[i]
            if (acc + snapshot.duration() > timestamp) break
            acc += snapshot.duration()
        }

        val snapshot = snapshots[snapshotIndex]

        /*val actionTime = timestamp - acc
        val actionFraction = animationTimeFraction(actionTime, snapshot.actionsDuration())
        acc += snapshot.actionsDuration()*/

        val movingTime = timestamp - acc
        val movingFraction = animationTimeFraction(movingTime, snapshot.movementDuration())
        acc += snapshot.movementDuration()

        val deathRayTime = timestamp - acc
        val deathRayFraction = animationTimeFraction(deathRayTime, snapshot.deathRaysDuration())
        acc += snapshot.deathRaysDuration()

        val hexRemovalTime = timestamp - acc
        val hexRemovalFraction = animationTimeFraction(hexRemovalTime, snapshot.hexRemovalDuration())

        state.snapshotIndex = snapshotIndex
        state.movingFraction = movingFraction
        state.deathRayFraction = deathRayFraction
        state.hexRemovalFraction = hexRemovalFraction
    }

    private fun animationTimeFraction(time: Long, animationDuration: Int): Float {
        return if (animationDuration == 0) 0f
        else if (time < 0) 0f
        else if (time > animationDuration) 1f
        else time.toFloat() / animationDuration.toFloat()
    }

    private data class FrameState(var snapshotIndex: Int = 0, /*val actionFraction: Float,*/ var movingFraction: Float = 0f,
                          var deathRayFraction: Float = 0f, var hexRemovalFraction: Float = 0f)

    companion object {
        private const val TAG = "BattleGraphics"
        const val TIME_BETWEEN_FRAMES_MS: Long = 20
    }
}

