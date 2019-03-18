package bav.onecell.cellslist.cellselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.cellslist.ScenesModule
import bav.onecell.common.Common
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartAfterBelos
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartAfterDrunkards
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartAfterGopniks
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartAfterKatofiPonu
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartAfterNihteribs
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartAfterOmikhli
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartAfterSkilos
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartAnalafro
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartBattleWithBelos
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartBattleWithDrunkards
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartBattleWithMage
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartBattleWithGopniks
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartBattleWithKatofiPonu
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartBattleWithNikhteribs
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartBattleWithOmikhli
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartBattleWithSkilos
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartBeforeSkilos
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartEnkefalio
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartFinalAct
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartGonato
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartIntroduction
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartKardia
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartKilia
import kotlinx.android.synthetic.main.fragment_scenes.buttonStartLaimo
import javax.inject.Inject

class ScenesFragment : Fragment() {

    @Inject lateinit var gameState: Common.GameState

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scenes, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        inject()

        buttonStartIntroduction.setOnClickListener { navigateTo(it, R.id.cutSceneIntroduction) }
        buttonStartBattleWithGopniks.setOnClickListener { navigateTo(it, R.id.battleGopniks) }
        buttonStartAfterGopniks.setOnClickListener { navigateTo(it, R.id.cutSceneAfterGopniks) }

        buttonStartBeforeSkilos.setOnClickListener { navigateTo(it, R.id.cutSceneBeforeSkilos) }
        buttonStartBattleWithSkilos.setOnClickListener { navigateTo(it, R.id.battleSkilos) }
        buttonStartAfterSkilos.setOnClickListener { navigateTo(it, R.id.cutSceneAfterSkilos) }

        buttonStartGonato.setOnClickListener { navigateTo(it, R.id.cutSceneGonato) }
        buttonStartBattleWithBelos.setOnClickListener { navigateTo(it, R.id.battleBelos) }
        buttonStartAfterBelos.setOnClickListener { navigateTo(it, R.id.cutSceneAfterBelos) }

        buttonStartAnalafro.setOnClickListener { navigateTo(it, R.id.cutSceneAnalafro) }
        buttonStartBattleWithOmikhli.setOnClickListener { navigateTo(it, R.id.battleOmikhli) }
        buttonStartAfterOmikhli.setOnClickListener { navigateTo(it, R.id.cutSceneAfterOmikhli) }

        buttonStartKilia.setOnClickListener { navigateTo(it, R.id.cutSceneKilia) }
        buttonStartBattleWithNikhteribs.setOnClickListener { navigateTo(it, R.id.battleNikhteribs) }
        buttonStartAfterNihteribs.setOnClickListener { navigateTo(it, R.id.cutSceneAfterNikhteribs) }

        buttonStartKardia.setOnClickListener { navigateTo(it, R.id.cutSceneKardia) }
        buttonStartBattleWithDrunkards.setOnClickListener { navigateTo(it, R.id.battleDrunkards) }
        buttonStartAfterDrunkards.setOnClickListener { navigateTo(it, R.id.cutSceneAfterDrunkards) }

        buttonStartLaimo.setOnClickListener { navigateTo(it, R.id.cutSceneLaimo) }
        buttonStartBattleWithKatofiPonu.setOnClickListener { navigateTo(it, R.id.battleKatofiPonu) }
        buttonStartAfterKatofiPonu.setOnClickListener { navigateTo(it, R.id.cutSceneAfterKatofiPonu) }

        buttonStartEnkefalio.setOnClickListener { navigateTo(it, R.id.cutSceneEnkefalio) }
        buttonStartBattleWithMage.setOnClickListener { navigateTo(it, R.id.battleEnkefalio) }
        buttonStartFinalAct.setOnClickListener { navigateTo(it, R.id.cutSceneFinalAct) }
    }
    
    private fun navigateTo(view: View, sceneId: Int) {
        gameState.setCurrentFrame(0)
        gameState.setIgnoreCutSceneShownStatus(true)
        view.findNavController().navigate(sceneId)
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(ScenesModule())
                .inject(this)
    }

    companion object {
        private const val TAG = "ScenesFragment"
    }
}
