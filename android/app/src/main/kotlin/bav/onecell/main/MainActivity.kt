package bav.onecell.main

import android.os.Bundle
import android.app.Activity
import bav.onecell.model.CanvasView
import bav.onecell.model.hexes.Hex

import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hexes: MutableSet<Hex> = mutableSetOf()
        val mapRadius = 4
        for (q in -mapRadius..mapRadius) {
            val r1: Int = max(-mapRadius, -q - mapRadius)
            val r2: Int = min(mapRadius, -q + mapRadius)
            for (r in r1..r2) {
                hexes.add(Hex(q, r, -q - r))
            }
        }

        val canvasView = CanvasView(this, hexes)
        setContentView(canvasView)
    }
}