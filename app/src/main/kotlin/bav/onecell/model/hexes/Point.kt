package bav.onecell.model.hexes

data class Point(var x: Double = 0.0, var y: Double = 0.0) {
    fun copy(p: Point) {
        x = p.x
        y = p.y
    }
}
