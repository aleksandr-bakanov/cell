package bav.onecell.battle

import android.util.Log
import bav.onecell.common.Consts
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.BattleFieldSnapshot
import bav.onecell.model.BattleInfo
import bav.onecell.model.battle.FrameGraphics
import bav.onecell.model.cell.logic.Action
import bav.onecell.model.hexes.Point
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BattleGraphics(private val drawUtils: DrawUtils) : Battle.FramesFactory {

    private val framesProvider: PublishSubject<Map<Long, FrameGraphics>> = PublishSubject.create()

    override fun framesProvider(): Observable<Map<Long, FrameGraphics>> = framesProvider

    override fun generateFrames(battleInfo: BattleInfo) {
        GlobalScope.launch {
            val frames = mutableMapOf<Long, FrameGraphics>()
            val battleDuration = battleInfo.snapshots.sumBy { it.duration() }.toLong()
            val isFog = battleInfo.isFog
            val isBattleWon = battleInfo.winnerGroupId == Consts.HERO_GROUP_ID

            var prev = 0.0
            var curr = 0.0

            for (timestamp in 0..battleDuration step TIME_BETWEEN_FRAMES_MS) {

                val part = timestamp.toDouble() / battleDuration.toDouble()
                if (part >= 0.9) curr = 0.9
                else if (part >= 0.8) curr = 0.8
                else if (part >= 0.7) curr = 0.7
                else if (part >= 0.6) curr = 0.6
                else if (part >= 0.5) curr = 0.5
                else if (part >= 0.4) curr = 0.4
                else if (part >= 0.3) curr = 0.3
                else if (part >= 0.2) curr = 0.2
                else if (part >= 0.1) curr = 0.1

                if (curr > prev) {
                    prev = curr
                    Log.d(TAG, "calculating graphics: ${prev * 100.0}%")
                }

                val frameState = getFrameState(battleInfo.snapshots, timestamp)
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

                // Death rays
                // battleCanvasView.deathRayFraction = if (snapshot.deathRays.isNotEmpty()) frameState.deathRayFraction else 0f

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
                snapshot.cells.forEach { cell ->
                    val cellGraphicalPoints = drawUtils.getCellGraphicalRepresentation(cell)
                    cellGraphicalPoints?.let { cellPoints ->
                        cellPoints.lifeHexes?.let {
                            if (frameGraphics.lifeHexes == null) frameGraphics.lifeHexes = mutableListOf()
                            frameGraphics.lifeHexes?.addAll(it)
                        }
                        cellPoints.attackHexes?.let {
                            if (frameGraphics.attackHexes == null) frameGraphics.attackHexes = mutableListOf()
                            frameGraphics.attackHexes?.addAll(it)
                        }
                        cellPoints.energyHexes?.let {
                            if (frameGraphics.energyHexes == null) frameGraphics.energyHexes = mutableListOf()
                            frameGraphics.energyHexes?.addAll(it)
                        }
                        cellPoints.deathRayHexes?.let {
                            if (frameGraphics.deathRayHexes == null) frameGraphics.deathRayHexes = mutableListOf()
                            frameGraphics.deathRayHexes?.addAll(it)
                        }
                        cellPoints.omniBulletHexes?.let {
                            if (frameGraphics.omniBulletHexes == null) frameGraphics.omniBulletHexes = mutableListOf()
                            frameGraphics.omniBulletHexes?.addAll(it)
                        }
                        cellPoints.outline?.let {
                            val isCellFriendly = cell.data.groupId == Consts.MAIN_CHARACTERS_GROUP_ID
                            if (isCellFriendly) {
                                if (frameGraphics.friendsOutline == null) frameGraphics.friendsOutline = mutableListOf()
                                frameGraphics.friendsOutline?.addAll(it)
                            }
                            else {
                                if (frameGraphics.enemiesOutline == null) frameGraphics.enemiesOutline = mutableListOf()
                                frameGraphics.enemiesOutline?.addAll(it)
                            }
                        }
                    }
                }

                // Corpses cells
                snapshot.corpses.forEach { corpse ->
                    val cellGraphicalPoints = drawUtils.getCellGraphicalRepresentation(corpse)
                    cellGraphicalPoints?.let { cellPoints ->
                        cellPoints.lifeHexes?.let {
                            if (frameGraphics.corpseLifeHexes == null) frameGraphics.corpseLifeHexes = mutableListOf()
                            frameGraphics.corpseLifeHexes?.addAll(it)
                        }
                        cellPoints.attackHexes?.let {
                            if (frameGraphics.corpseAttackHexes == null) frameGraphics.corpseAttackHexes = mutableListOf()
                            frameGraphics.corpseAttackHexes?.addAll(it)
                        }
                        cellPoints.energyHexes?.let {
                            if (frameGraphics.corpseEnergyHexes == null) frameGraphics.corpseEnergyHexes = mutableListOf()
                            frameGraphics.corpseEnergyHexes?.addAll(it)
                        }
                        cellPoints.deathRayHexes?.let {
                            if (frameGraphics.corpseDeathRayHexes == null) frameGraphics.corpseDeathRayHexes = mutableListOf()
                            frameGraphics.corpseDeathRayHexes?.addAll(it)
                        }
                        cellPoints.omniBulletHexes?.let {
                            if (frameGraphics.corpseOmniBulletHexes == null) frameGraphics.corpseOmniBulletHexes = mutableListOf()
                            frameGraphics.corpseOmniBulletHexes?.addAll(it)
                        }
                    }
                }

                // Death rays

                // Bullets

                // Field of view

                // Save frame graphics
                frames[timestamp] = frameGraphics
            }

            framesProvider.onNext(frames)
        }
    }

    private fun getFrameState(snapshots: List<BattleFieldSnapshot>, timestamp: Long): FrameState {
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

        return FrameState(snapshotIndex, /*actionFraction,*/ movingFraction, deathRayFraction, hexRemovalFraction)
    }

    private fun animationTimeFraction(time: Long, animationDuration: Int): Float {
        return if (animationDuration == 0) 0f
        else if (time < 0) 0f
        else if (time > animationDuration) 1f
        else time.toFloat() / animationDuration.toFloat()
    }

    private data class FrameState(val snapshotIndex: Int, /*val actionFraction: Float,*/ val movingFraction: Float,
                          val deathRayFraction: Float, val hexRemovalFraction: Float)

    companion object {
        private const val TAG = "BattleGraphics"
        const val TIME_BETWEEN_FRAMES_MS: Long = 20
    }
}

