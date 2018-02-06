package bav.cellandroidclient.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import bav.cellandroidclient.engine.CanvasView
import bav.cellandroidclient.engine.Hex

import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {
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