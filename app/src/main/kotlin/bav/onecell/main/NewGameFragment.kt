package bav.onecell.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.databinding.FragmentNewGameBinding
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Data
import javax.inject.Inject

class NewGameFragment : Fragment(), NewGame.View {

    @Inject lateinit var gameState: Common.GameState
    @Inject lateinit var cellRepo: RepositoryContract.CellRepo
    @Inject lateinit var analytics: Common.Analytics

    private var _binding: FragmentNewGameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNewGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()
        initiateButtons()
    }

    override fun onResume() {
        super.onResume()
        analytics.setCurrentScreen(requireActivity(), SCREEN_NAME, this::class.qualifiedName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initiateButtons() {
        binding.buttonDeny.setOnClickListener { view ->
            view.findNavController().popBackStack()
        }
        binding.buttonConfirm.setOnClickListener { view ->
            resetGameState()
            view.findNavController().navigate(R.id.action_newGameFragment_to_cutSceneIntroduction)
        }
    }

    private fun resetGameState() {
        /*for (decision in arrayListOf(
                Common.GameState.BATTLE_LOGIC_AVAILABLE,
                Common.GameState.ATTACK_HEXES_AVAILABLE,
                Common.GameState.ENERGY_HEXES_AVAILABLE,
                Common.GameState.DEATH_RAY_HEXES_AVAILABLE,
                Common.GameState.OMNI_BULLET_HEXES_AVAILABLE,
                Common.GameState.HEX_TRANSFORMATION_AVAILABLE,
                Common.GameState.ZOI_AVAILABLE,
                Common.GameState.AIMA_AVAILABLE,
                Common.GameState.ALL_CHARACTERS_AVAILABLE)) {
            gameState.setDecision(decision, false)
        }
        // Restore initial state of heroes
        val cellJsons = resources.getStringArray(R.array.cell_descriptions)
        for (hero in arrayListOf(cellJsons[0], cellJsons[1], cellJsons[2])) {
            cellRepo.storeCell(Data.fromJson(hero))
        }*/

        gameState.dropGameState()
        cellRepo.restoreCellRepository()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(MainModule(null))
                .inject(this)
    }

    companion object {
        private const val SCREEN_NAME = "New game warning"
    }
}
