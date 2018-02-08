package bav.onecell.main

import bav.onecell.model.CellRepository
import javax.inject.Inject

class MainPresenter(private val view: Main.View): Main.Presenter {

    @Inject
    lateinit var cellRepository: CellRepository

}