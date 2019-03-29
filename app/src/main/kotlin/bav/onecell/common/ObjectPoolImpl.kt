package bav.onecell.common

import bav.onecell.common.view.DrawUtils
import bav.onecell.model.battle.FrameGraphics

class ObjectPoolImpl(
        private val frameGraphics: FrameGraphics = FrameGraphics(),
        private val cellGraphicalPoints: DrawUtils.CellGraphicalPoints = DrawUtils.CellGraphicalPoints()): Common.ObjectPool {

    override fun getFrameGraphics(): FrameGraphics = frameGraphics
    override fun getCellGraphicalRepresentation(): DrawUtils.CellGraphicalPoints = cellGraphicalPoints
}