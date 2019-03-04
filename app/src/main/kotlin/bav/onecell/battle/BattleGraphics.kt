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
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class BattleGraphics(
        private val drawUtils: DrawUtils,
        private val hexMath: HexMath) : Battle.FramesFactory {

    private val framesProvider: PublishSubject<Map<Long, FrameGraphics>> = PublishSubject.create()
    private val progressProvider: PublishSubject<Int> = PublishSubject.create()

    override fun framesProvider(): Observable<Map<Long, FrameGraphics>> = framesProvider
    override fun progressProvider(): Observable<Int> = progressProvider

    private var previous: Long = 0

    private fun checkProgress(current: Long, total: Long) {
        if ((current - previous).toFloat() / total.toFloat() > 0.01f) {
            previous = current
            progressProvider.onNext(((previous.toFloat() / total.toFloat()) * 100f).toInt())
        }
    }

    override fun generateFrames(battleInfo: BattleInfo): Job {
        return GlobalScope.launch {
            val frames = mutableMapOf<Long, FrameGraphics>()
            val battleDuration = battleInfo.snapshots.sumBy { it.duration() }.toLong()
            val isFog = battleInfo.isFog
            val isBattleWon = battleInfo.winnerGroupId == Consts.HERO_GROUP_ID
            previous = 0

            val frameState = FrameState()

            for (timestamp in 0..battleDuration step TIME_BETWEEN_FRAMES_MS) {
                yield()
                checkProgress(timestamp, battleDuration)

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
                val frameGraphics = FrameGraphics()

                // Living cells
                if (snapshot.cells.isNotEmpty()) {
                    frameGraphics.livingCells = mutableListOf()
                    snapshot.cells.forEach { cell ->
                        drawUtils.getCellGraphicalRepresentation(cell)?.let {
                            frameGraphics.livingCells?.add(it)
                        }
                    }
                }

                // Corpse cells
                if (snapshot.corpses.isNotEmpty()) {
                    frameGraphics.corpses = mutableListOf()
                    snapshot.corpses.forEach { corpse ->
                        drawUtils.getCellGraphicalRepresentation(corpse)?.let {
                            frameGraphics.corpses?.add(it)
                        }
                    }
                }

                // Death rays
                if (snapshot.deathRays.isNotEmpty()) {
                    frameGraphics.deathRays = mutableListOf()
                    snapshot.deathRays.forEach { ray ->
                        frameGraphics.deathRays?.add(hexMath.hexToPixel(Layout.UNIT, ray.first))
                        frameGraphics.deathRays?.add(hexMath.hexToPixel(Layout.UNIT, ray.second))
                    }
                    frameGraphics.deathRaysAlpha = if (frameState.deathRayFraction < 0.5) {
                        (frameState.deathRayFraction * 2f * 255f).toInt()
                    } else {
                        ((1f - frameState.deathRayFraction) * 2f * 255f).toInt()
                    }
                }

                // Bullets
                if (snapshot.bullets.isNotEmpty()) {
                    frameGraphics.bullets = mutableListOf()
                    snapshot.bullets.forEach { bullet ->
                        frameGraphics.bullets?.add(drawUtils.getBulletPath(bullet, Layout.UNIT))
                    }
                }

                // Field of view
                // In case last frame and battle is won fog doesn't show
                if (isFog && !(timestamp == battleDuration && isBattleWon)) {
                    frameGraphics.fieldOfView = getObservableAreaPath(snapshot.cells)
                }

                // Save frame graphics
                frames[timestamp] = frameGraphics
            }

            framesProvider.onNext(frames)
        }
    }

    /// TODO: optimize area definition (in terms of allocated memory)
    private fun getObservableAreaPath(cells: List<Cell>): Path {
        // Get area observed by cells with group id = 0 only, i.e. main heroes
        val path = Path()
        cells.forEach { cell ->
            if (cell.data.groupId == Consts.MAIN_CHARACTERS_GROUP_ID) {
                val cellViewArea = mutableSetOf<Hex>()
                cell.data.hexes.values.forEach { hex ->
                    cellViewArea.add(hexMath.add(hex, cell.data.origin))
                }
                for (i in 0 until cell.data.viewDistance) {
                    val nextLayer = mutableSetOf<Hex>()
                    cellViewArea.forEach { hex ->
                        nextLayer.addAll(hexMath.hexNeighbors(hex).subtract(cellViewArea))
                    }
                    cellViewArea.addAll(nextLayer)
                }
                cellViewArea.forEach { hex ->
                    val origin = hexMath.hexToPixel(Layout.UNIT, hex)
                    drawUtils.offsetPoint(origin, cell.animationData.moveDirection, cell.animationData.movingFraction, Layout.UNIT)
                    path.addCircle(origin.x.toFloat(), origin.y.toFloat(), Layout.UNIT.size.x.toFloat(), Path.Direction.CW)
                }
            }
        }
        path.close()
        return path
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

