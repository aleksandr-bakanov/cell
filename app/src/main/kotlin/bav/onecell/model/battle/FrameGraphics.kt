package bav.onecell.model.battle

import android.graphics.Path
import bav.onecell.model.hexes.Point

/**
 * Состояние поля боя в определенный момент времени. Хексы всех клеток сгруппированы по типу.
 * Также как обводки.
 */
class FrameGraphics(
        // Living cells
        // Each six points represent one hex
        var lifeHexes: MutableList<Path>? = null,
        var attackHexes: MutableList<Path>? = null,
        var energyHexes: MutableList<Path>? = null,
        var deathRayHexes: MutableList<Path>? = null,
        var omniBulletHexes: MutableList<Path>? = null,

        // Corpses
        var corpseLifeHexes: MutableList<Path>? = null,
        var corpseAttackHexes: MutableList<Path>? = null,
        var corpseEnergyHexes: MutableList<Path>? = null,
        var corpseDeathRayHexes: MutableList<Path>? = null,
        var corpseOmniBulletHexes: MutableList<Path>? = null,

        // Outlines
        var friendsOutline: MutableList<Point>? = null,
        var enemiesOutline: MutableList<Point>? = null,

        // Death rays
        var deathRays: MutableList<Point>? = null,

        // Bullets
        var bullets: MutableList<Point>? = null,

        // Fog
        var fieldOfView: Path? = null
)