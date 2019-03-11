package bav.onecell.common

import stormpot.Allocator
import stormpot.Poolable
import stormpot.Slot

class PoolsImpl : Common.Pools {
    override fun initialize() {

    }
}

class PointPoolable(private val slot: Slot?, var x: Double = 0.0, var y: Double = 0.0): Poolable {
    override fun release() {
        slot?.release(this)
    }
}

class PointAllocator : Allocator<PointPoolable> {
    override fun allocate(slot: Slot?): PointPoolable = PointPoolable(slot)
    override fun deallocate(poolable: PointPoolable?) {}
}
