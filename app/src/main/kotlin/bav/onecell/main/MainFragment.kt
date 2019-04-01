package bav.onecell.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlinx.android.synthetic.main.fragment_main.buttonSendReport
import javax.inject.Inject
import android.content.Intent
import android.net.Uri


class MainFragment : Fragment(), Main.View {

    @Inject lateinit var presenter: Main.Presenter
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

        presenter.setDebugDecisions()

        if (presenter.isGameFinished()) {
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
        buttonSendReport.setOnClickListener { view ->
            presenter.sendBugReport()
        }

        lastNavDestination = presenter.getLastNavDestinationId()
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

    override fun informAboutBugReportFilePath(path: String, content: String) {
        Toast.makeText(requireContext(), "Report saved to $path", Toast.LENGTH_LONG).show()
        val data = "mailto:bakanov.aleksandr@gmail.com?subject=${Uri.encode("Kittaro's bug report")}&body=${Uri.encode(content)}"
        val uri = Uri.parse(data)
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = uri
        startActivity(Intent.createChooser(intent, "Send report"))
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(MainModule(this))
                .inject(this)
    }

    companion object {
        private const val TAG = "MainFragment"
        private const val SCREEN_NAME = "Main menu"
    }
}
