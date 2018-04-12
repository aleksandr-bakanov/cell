package bav.onecell.model

import bav.onecell.model.hexes.HexMath

class CellRepository(
        private val hexMath: HexMath,
        private val cells: ArrayList<Cell> = arrayListOf()) : RepositoryContract.CellRepo {

    override fun cellsCount(): Int = cells.size

    override fun addCell(cell: Cell) {
        cells.add(cell)
    }

    override fun createNewCell() {
        val cell = Cell(hexMath)
        addCell(cell)
    }

    override fun getCell(index: Int): Cell? {
        return if (index in 0..(cells.size - 1)) cells[index] else null
    }
}