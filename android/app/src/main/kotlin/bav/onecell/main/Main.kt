package bav.onecell.main

interface Main {

  interface View {

  }

  interface Presenter {
    /**
     * Creates new cell an stores it in cell repository
     */
    fun createNewCell()

    /**
     * Provides CellListAdapter instance for RecyclerView
     *
     * @return CellListAdapter instance
     */
    fun provideCellListAdapter(): CellListAdapter
  }
}