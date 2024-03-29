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
import bav.onecell.common.Consts
import bav.onecell.databinding.FragmentScenesBinding
import javax.inject.Inject

class ScenesFragment : Fragment() {

    @Inject lateinit var gameState: Common.GameState

    private var _binding: FragmentScenesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentScenesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()
        initiateListeners()
    }

    override fun onResume() {
        super.onResume()
        initiateButtonsVisibility()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initiateListeners() {
        with (binding) {
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
            buttonStartBattleWithNikhteribs.setOnClickListener {
                navigateTo(it, R.id.battleNikhteribs)
            }
            buttonStartAfterNihteribs.setOnClickListener {
                navigateTo(it, R.id.cutSceneAfterNikhteribs)
            }

            buttonStartKardia.setOnClickListener { navigateTo(it, R.id.cutSceneKardia) }
            buttonStartBattleWithDrunkards.setOnClickListener {
                navigateTo(it, R.id.battleDrunkards)
            }
            buttonStartAfterDrunkards.setOnClickListener {
                navigateTo(it, R.id.cutSceneAfterDrunkards)
            }

            buttonStartLaimo.setOnClickListener { navigateTo(it, R.id.cutSceneLaimo) }
            buttonStartBattleWithKatofiPonu.setOnClickListener {
                navigateTo(it, R.id.battleKatofiPonu)
            }
            buttonStartAfterKatofiPonu.setOnClickListener {
                navigateTo(it, R.id.cutSceneAfterKatofiPonu)
            }

            buttonStartEnkefalio.setOnClickListener { navigateTo(it, R.id.cutSceneEnkefalio) }
            buttonStartBattleWithMage.setOnClickListener { navigateTo(it, R.id.battleEnkefalio) }
            buttonStartFinalAct.setOnClickListener { navigateTo(it, R.id.cutSceneFinalAct) }
        }
    }

    private fun initiateButtonsVisibility() {
        with (binding) {
            buttonStartIntroduction.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.INTRODUCTION.value)) View.VISIBLE else View.GONE
            buttonStartBattleWithGopniks.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.BATTLE_GOPNIKS.value)) View.VISIBLE else View.GONE
            buttonStartAfterGopniks.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.AFTER_GOPNIKS.value)) View.VISIBLE else View.GONE
            buttonStartBeforeSkilos.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.BEFORE_SKILOS.value)) View.VISIBLE else View.GONE
            buttonStartBattleWithSkilos.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.BATTLE_SKILOS.value)) View.VISIBLE else View.GONE
            buttonStartAfterSkilos.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.AFTER_SKILOS.value)) View.VISIBLE else View.GONE
            buttonStartGonato.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.GONATO.value)) View.VISIBLE else View.GONE
            buttonStartBattleWithBelos.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.BATTLE_BELOS.value)) View.VISIBLE else View.GONE
            buttonStartAfterBelos.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.AFTER_BELOS.value)) View.VISIBLE else View.GONE
            buttonStartAnalafro.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.ANALAFRO.value)) View.VISIBLE else View.GONE
            buttonStartBattleWithOmikhli.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.BATTLE_OMIKHLI.value)) View.VISIBLE else View.GONE
            buttonStartAfterOmikhli.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.AFTER_OMIKHLI.value)) View.VISIBLE else View.GONE
            buttonStartKilia.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.KILIA.value)) View.VISIBLE else View.GONE
            buttonStartBattleWithNikhteribs.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.BATTLE_NIKHTERIBS.value)) View.VISIBLE else View.GONE
            buttonStartAfterNihteribs.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.AFTER_NIKHTERIBS.value)) View.VISIBLE else View.GONE
            buttonStartKardia.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.KARDIA.value)) View.VISIBLE else View.GONE
            buttonStartBattleWithDrunkards.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.BATTLE_DRUNKARDS.value)) View.VISIBLE else View.GONE
            buttonStartAfterDrunkards.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.AFTER_DRUNKARDS.value)) View.VISIBLE else View.GONE
            buttonStartLaimo.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.LAIMO.value)) View.VISIBLE else View.GONE
            buttonStartBattleWithKatofiPonu.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.BATTLE_KATOFI_PONU.value)) View.VISIBLE else View.GONE
            buttonStartAfterKatofiPonu.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.AFTER_KATOFI_PONU.value)) View.VISIBLE else View.GONE
            buttonStartEnkefalio.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.ENKEFALIO.value)) View.VISIBLE else View.GONE
            buttonStartBattleWithMage.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.BATTLE_ENKEFALIO.value)) View.VISIBLE else View.GONE
            buttonStartFinalAct.visibility = if (gameState.isSceneAppeared(
                            Consts.SceneId.FINAL_ACT.value)) View.VISIBLE else View.GONE
        }
    }

    private val battleSceneIds = arrayListOf(R.id.battleGopniks, R.id.battleSkilos, R.id.battleBelos,
                                             R.id.battleOmikhli, R.id.battleNikhteribs, R.id.battleDrunkards,
                                             R.id.battleKatofiPonu, R.id.battleEnkefalio)

    private fun navigateTo(view: View, sceneId: Int) {
        if (!battleSceneIds.contains(sceneId)) {
            gameState.setCurrentFrame(0)
            gameState.setIgnoreCutSceneShownStatus(true)
        }
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
