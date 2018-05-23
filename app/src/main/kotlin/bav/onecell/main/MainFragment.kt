package bav.onecell.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.OneCellApplication
import bav.onecell.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_main.buttonGoToBattle
import kotlinx.android.synthetic.main.fragment_main.buttonShowCells
import javax.inject.Inject

class MainFragment : Fragment(), Main.View {

    @Inject
    lateinit var presenter: Main.Presenter
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        buttonGoToBattle.setOnClickListener { presenter.openPreBattleView() }
        buttonShowCells.setOnClickListener { presenter.openCellsListView() }
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
