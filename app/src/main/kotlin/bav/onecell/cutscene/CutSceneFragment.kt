package bav.onecell.cutscene

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.OneCellApplication
import bav.onecell.R
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class CutSceneFragment : Fragment(), CutScene.View {

    @Inject lateinit var presenter: CutScene.Presenter
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cut_scene, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(CutSceneModule())
                .inject(this)
    }

    companion object {
        private const val TAG = "CutSceneFragment"
        @JvmStatic
        fun newInstance() = CutSceneFragment()
    }
}
