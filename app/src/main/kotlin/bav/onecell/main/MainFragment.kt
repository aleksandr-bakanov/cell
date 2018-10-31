package bav.onecell.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_main.buttonContinueGame
import kotlinx.android.synthetic.main.fragment_main.buttonExitGame
import kotlinx.android.synthetic.main.fragment_main.buttonGoToBattle
import kotlinx.android.synthetic.main.fragment_main.buttonHeroScreen
import kotlinx.android.synthetic.main.fragment_main.buttonNewGame
import javax.inject.Inject

class MainFragment : Fragment(), Main.View {

    @Inject
    lateinit var presenter: Main.Presenter
    private val disposables = CompositeDisposable()
    private var lastNavDestination: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        buttonGoToBattle.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_mainFragment_to_cellsForBattleFragment)
        }
        buttonNewGame.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_mainFragment_to_cutSceneIntroduction)
        }
        buttonHeroScreen.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_mainFragment_to_heroScreen)
        }
        buttonExitGame.setOnClickListener { requireActivity().finish() }
        buttonContinueGame.setOnClickListener {
            if (lastNavDestination != 0) it.findNavController().navigate(lastNavDestination)
        }

        (requireActivity() as? Main.NavigationInfoProvider)?.let {
            disposables.add(it.provideLastDestination().subscribe { destination ->
                lastNavDestination = destination
                buttonContinueGame.visibility = if (lastNavDestination == 0) View.GONE else View.VISIBLE
            })
        }
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

    companion object {
        private const val TAG = "MainFragment"
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}
