package bav.onecell.model.hexes

data class Layout(val orientation: Orientation, var size: Point, var origin: Point) {
    companion object {
        val DUMMY = Layout(Orientation.LAYOUT_POINTY, Point(50.0, 50.0), Point())
    }
}