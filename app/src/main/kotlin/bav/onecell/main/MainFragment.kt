package bav.onecell.main

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
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
        Log.d(TAG, "$this onCreateView")
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "$this onActivityCreated")
        inject()

        buttonGoToBattle.setOnClickListener { presenter.openPreBattleView() }
        buttonShowCells.setOnClickListener { presenter.openCellsListView() }
    }

    override fun onDestroyView() {
        Log.d(TAG, "$this onDestroyView")
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, "$this onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "$this onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "$this onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "$this onResume")
    }

    override fun onPause() {
        Log.d(TAG, "$this onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "$this onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "$this onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG, "$this onDetach")
        super.onDetach()
    }
}
