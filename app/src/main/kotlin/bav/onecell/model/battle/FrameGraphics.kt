package bav.onecell.model.battle

import android.graphics.Path
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.hexes.Point

/**
 * Состояние поля боя в определенный момент времени.
 */
class FrameGraphics(
        // Living cells
        var livingCells: MutableList<DrawUtils.CellGraphicalPoints>? = null,

        // Corpses
        var corpses: MutableList<DrawUtils.CellGraphicalPoints>? = null,

        // Death rays
        var deathRays: MutableList<Point>? = null,
        var deathRaysAlpha: Int = 0,

        // Bullets
        var bullets: MutableList<Path>? = null,

        // Fog
        var fieldOfView: Path? = null) {

    fun clear() {
        livingCells?.forEach { it.clear() }
        corpses?.forEach { it.clear() }
        bullets?.forEach { it.reset() }
        fieldOfView?.reset()

        livingCells?.clear()
        corpses?.clear()
        deathRays?.clear()
        bullets?.clear()

        livingCells = null
        corpses = null
        deathRays = null
        bullets = null
        fieldOfView = null
    }
}
