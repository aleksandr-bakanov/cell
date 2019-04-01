package bav.onecell.main

import android.os.Environment
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.model.RepositoryContract
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainPresenter(
        private val view: Main.View?,
        private val gameState: Common.GameState,
        private val analytics: Common.Analytics,
        private val cellRepo: RepositoryContract.CellRepo) : Main.Presenter {

    override fun setDebugDecisions() {
        gameState.setDecision(Common.GameState.BATTLE_LOGIC_AVAILABLE, true)
        gameState.setDecision(Common.GameState.ATTACK_HEXES_AVAILABLE, true)
        gameState.setDecision(Common.GameState.ENERGY_HEXES_AVAILABLE, true)
        gameState.setDecision(Common.GameState.DEATH_RAY_HEXES_AVAILABLE, true)
        gameState.setDecision(Common.GameState.OMNI_BULLET_HEXES_AVAILABLE, true)
        gameState.setDecision(Common.GameState.HEX_TRANSFORMATION_AVAILABLE, true)
        gameState.setDecision(Common.GameState.ZOI_AVAILABLE, true)
        gameState.setDecision(Common.GameState.ALL_CHARACTERS_AVAILABLE, true)
        gameState.setDecision(Common.GameState.GAME_OVER, true)
    }

    override fun isGameFinished(): Boolean = gameState.isDecisionPositive(Common.GameState.GAME_OVER)

    override fun getLastNavDestinationId(): Int = gameState.getLastNavDestinationId()

    override fun sendBugReport() {
        if (isExternalStorageWritable()) {
            val file = File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "kittaro_message_cells.txt")
            file.createNewFile()
            FileOutputStream(file).use { fos ->
                for (i in arrayOf(Consts.KITTARO_INDEX, Consts.ZOI_INDEX, Consts.AIMA_INDEX)) {
                    cellRepo.getCell(i)?.let {
                        fos.write(it.data.toJson().toByteArray(Charsets.UTF_8))
                    }
                    fos.write("\n\n\n".toByteArray())
                }
                fos.close()
            }
            val reportContent = FileInputStream(file).bufferedReader().use { it.readText() }
            view?.informAboutBugReportFilePath(file.absolutePath, reportContent)
        }
    }

    //region Private
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
    //endregion

    companion object {
        private const val TAG = "MainPresenter"
    }
}