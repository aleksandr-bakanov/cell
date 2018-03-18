package bav.onecell.model

data class CellRepository(private val cells: ArrayList<Cell> = arrayListOf()) :
    RepositoryContract.CellRepo {

    override fun cellsCount(): Int = cells.size

    override fun addCell(cell: Cell) {
        cells.add(cell)
    }

    override fun getCell(index: Int): Cell? {
        return if (index in 0..(cells.size - 1)) cells[index] else null
    }
}