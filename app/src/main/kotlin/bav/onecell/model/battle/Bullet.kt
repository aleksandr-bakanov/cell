package bav.onecell.model.battle

import bav.onecell.model.hexes.Hex

data class Bullet(val groupId: Int, val direction: Int, var timeToLive: Int, var origin: Hex) {

    fun clone(): Bullet = Bullet(groupId, direction, timeToLive, origin)

    companion object {
        const val OMNI_BULLET_TIMEOUT = 5
        const val OMNI_BULLET_RANGE = 5
    }
}
