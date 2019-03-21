package bav.onecell.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.common.extensions.visible
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_main.buttonContinueGame
import kotlinx.android.synthetic.main.fragment_main.buttonExitGame
import kotlinx.android.synthetic.main.fragment_main.buttonGoToScenes
import kotlinx.android.synthetic.main.fragment_main.buttonHeroScreen
import kotlinx.android.synthetic.main.fragment_main.buttonNewGame
import javax.inject.Inject

class MainFragment : Fragment(), Main.View {

    @Inject lateinit var presenter: Main.Presenter
    @Inject lateinit var gameState: Common.GameState
    @Inject lateinit var analytics: Common.Analytics

    private val disposables = CompositeDisposable()
    private var lastNavDestination: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        //setDebugDecisions()

        if (gameState.isDecisionPositive(Common.GameState.GAME_OVER)) {
            buttonGoToScenes.setOnClickListener { view ->
                view.findNavController().navigate(R.id.scenesFragment)
            }
            buttonGoToScenes.visibility = View.VISIBLE
            buttonHeroScreen.setOnClickListener { view ->
                view.findNavController().navigate(R.id.heroScreen)
            }
            buttonHeroScreen.visibility = View.VISIBLE
        }
        else {
            buttonGoToScenes.visibility = View.GONE
            buttonHeroScreen.visibility = View.GONE
        }

        buttonNewGame.setOnClickListener { view ->
            view.findNavController().navigate(R.id.newGameFragment)
        }
        buttonExitGame.setOnClickListener { requireActivity().finish() }
        buttonContinueGame.setOnClickListener {
            if (lastNavDestination != 0) it.findNavController().navigate(lastNavDestination)
        }

        lastNavDestination = gameState.getLastNavDestinationId()
        buttonContinueGame.visible = lastNavDestination != 0

        (requireActivity() as? Main.NavigationInfoProvider)?.let {
            disposables.add(it.provideLastDestination().subscribe { destination ->
                lastNavDestination = destination
                buttonContinueGame.visible = lastNavDestination != 0
            })
        }
    }

    override fun onResume() {
        super.onResume()
        analytics.setCurrentScreen(requireActivity(), SCREEN_NAME, this::class.qualifiedName)
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(MainModule())
                .inject(this)
    }

    private fun setDebugDecisions() {
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

    companion object {
        private const val TAG = "MainFragment"
        private const val SCREEN_NAME = "Main menu"
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}
