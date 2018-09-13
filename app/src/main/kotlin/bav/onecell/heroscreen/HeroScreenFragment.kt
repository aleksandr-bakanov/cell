package bav.onecell.heroscreen

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonMainMenu
import kotlinx.android.synthetic.main.fragment_hero_screen.editorCanvasView
import kotlinx.android.synthetic.main.fragment_hero_screen.textMoney
import javax.inject.Inject

class HeroScreenFragment: Fragment(), HeroScreen.View {

    @Inject lateinit var presenter: HeroScreen.Presenter
    @Inject lateinit var hexMath: HexMath
    @Inject lateinit var drawUtils: DrawUtils

    private val disposables = CompositeDisposable()

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hero_screen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()
        initiateCanvasView()

        buttonMainMenu.setOnClickListener { presenter.openMainMenu() }

        disposables.addAll(
                presenter.getCellProvider().subscribe {
                    editorCanvasView.cell = it
                    textMoney.text = resources.getString(R.string.text_money, it.data.money)
                    highlightTips(editorCanvasView.selectedCellType)

                    disposables.add(it.getMoneyProvider().subscribe { money ->
                        textMoney.text = resources.getString(R.string.text_money, money)
                    })
                },
                presenter.getBackgroundCellRadiusProvider().subscribe {
                    editorCanvasView.backgroundFieldRadius = it
                    editorCanvasView.invalidate()
                }
        )
        presenter.initialize(0)

    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }
    //endregion

    //region Overridden methods
    override fun highlightTips(type: Hex.Type) {
        editorCanvasView.tipHexes = presenter.getTipHexes(type)
        editorCanvasView.invalidate()
    }
    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(HeroScreenModule(this))
                .inject(this)
    }

    private fun initiateCanvasView() {
        editorCanvasView.hexMath = hexMath
        editorCanvasView.drawUtils = drawUtils
        editorCanvasView.presenter = presenter
    }
    //endregion

    companion object {
        private const val TAG = "EditorFragment"

        @JvmStatic
        fun newInstance(bundle: Bundle?): HeroScreenFragment {
            val fragment = HeroScreenFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
