package bav.onecell.model.battle

import android.graphics.Path
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.hexes.Point

/**
 * Состояние поля боя в определенный момент времени.
 */
class FrameGraphics(
        // Living cells
        var livingCells: MutableList<DrawUtils.CellGraphicalPoints> = mutableListOf(),
        var livingCellsIndex: Int = 0,

        // Corpses
        var corpses: MutableList<DrawUtils.CellGraphicalPoints> = mutableListOf(),
        var corpsesIndex: Int = 0,

        // Death rays
        var deathRays: MutableList<Point> = mutableListOf(),
        var deathRaysIndex: Int = 0,
        var deathRaysAlpha: Int = 0,

        // Bullets
        var bullets: MutableList<Path> = mutableListOf(),
        var bulletsIndex: Int = 0,

        // Fog
        var fieldOfView: Path = Path(),
        var fullFog: Boolean = false) {

    fun getLivingCell(): DrawUtils.CellGraphicalPoints {
        if (livingCells.size == livingCellsIndex) livingCells.add(DrawUtils.CellGraphicalPoints())
        val ret = livingCells[livingCellsIndex]
        livingCellsIndex++
        return ret
    }

    fun getCorpse(): DrawUtils.CellGraphicalPoints {
        if (corpses.size == corpsesIndex) corpses.add(DrawUtils.CellGraphicalPoints())
        val ret = corpses[corpsesIndex]
        corpsesIndex++
        return ret
    }

    fun getDeathRayPoint(): Point {
        if (deathRays.size == deathRaysIndex) deathRays.add(Point())
        val ret = deathRays[deathRaysIndex]
        deathRaysIndex++
        return ret
    }

    fun getBullet(): Path {
        if (bullets.size == bulletsIndex) bullets.add(Path())
        val ret = bullets[bulletsIndex]
        bulletsIndex++
        return ret
    }

    fun reset() {
        livingCellsIndex = 0
        corpsesIndex = 0
        deathRaysIndex = 0
        bulletsIndex = 0
        fieldOfView.reset()
        fullFog = false
    }
}
