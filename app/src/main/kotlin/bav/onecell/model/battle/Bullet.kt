package bav.onecell.model.battle

import bav.onecell.model.hexes.Hex

data class Bullet(val groupId: Int, val direction: Int, var timeToLive: Int, var origin: Hex,
                  var movingFraction: Float = 0f) {

    fun clone(): Bullet = Bullet(groupId, direction, timeToLive, origin, movingFraction)

    companion object {
        const val OMNI_BULLET_TIMEOUT = 3
        const val OMNI_BULLET_RANGE = 15
    }
}
