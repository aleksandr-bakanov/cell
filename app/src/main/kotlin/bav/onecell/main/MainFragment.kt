package bav.onecell.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
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
import bav.onecell.BuildConfig
import com.crashlytics.android.Crashlytics


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

        if (BuildConfig.DEBUG) {
            //presenter.setDebugDecisions()
        }

        if (presenter.isGameFinished()) {
            buttonHeroScreen.setOnClickListener { view ->
                view.findNavController().navigate(R.id.heroScreen)
            }
            buttonHeroScreen.visibility = View.VISIBLE
        }
        else {
            buttonHeroScreen.visibility = View.GONE
        }

        if (presenter.showScenesButton()) {
            buttonGoToScenes.setOnClickListener { view ->
                view.findNavController().navigate(R.id.scenesFragment)
            }
            buttonGoToScenes.visibility = View.VISIBLE
        }
        else {
            buttonGoToScenes.visibility = View.GONE
        }

        buttonNewGame.setOnClickListener { view ->
            if (lastNavDestination != 0) view.findNavController().navigate(R.id.newGameFragment)
            else view.findNavController().navigate(R.id.cutSceneIntroduction)
        }
        buttonExitGame.setOnClickListener { requireActivity().finish() }
        buttonContinueGame.setOnClickListener {
            if (lastNavDestination != 0) {
                try {
                    val navController = it.findNavController()
                    val node = navController.graph.findNode(lastNavDestination)
                    Crashlytics.log("MainFragment::buttonContinueGame::onClick id = $lastNavDestination; node.label = ${node?.label}")
                    navController.navigate(lastNavDestination)
                }
                catch (e: IllegalArgumentException) {
                    Log.e(TAG, e.toString())
                }
            }
        }
        buttonSendReport.setOnClickListener {
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

    override fun sendBugReport(content: String) {
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
