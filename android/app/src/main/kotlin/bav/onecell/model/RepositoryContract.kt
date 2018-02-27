package bav.onecell.model

interface RepositoryContract {

    interface CellRepo {

        fun cellsCount(): Int

        fun addCell(cell: Cell)

        fun getCell(index: Int): Cell?
    }
}