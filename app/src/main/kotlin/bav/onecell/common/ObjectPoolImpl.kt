package bav.onecell.common

import bav.onecell.model.battle.FrameGraphics

class ObjectPoolImpl(
        private val frameGraphics: FrameGraphics = FrameGraphics()): Common.ObjectPool {

    override fun getFrameGraphics(): FrameGraphics = frameGraphics
}