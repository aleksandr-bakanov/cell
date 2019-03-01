package bav.onecell.model.battle

import android.graphics.Path
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.hexes.Point

/**
 * Состояние поля боя в определенный момент времени. Хексы всех клеток сгруппированы по типу.
 * Также как обводки.
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
        var fieldOfView: Path? = null
)
