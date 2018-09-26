package bav.onecell.heroscreen

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.celllogic.conditions.ConditionsRecyclerViewAdapter
import bav.onecell.celllogic.picker.PickerRecyclerViewAdapter
import bav.onecell.celllogic.rules.RulesRecyclerViewAdapter
import bav.onecell.common.Common
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_hero_screen.aimaAvatar
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonDecreaseRulePriority
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonIncreaseRulePriority
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonMainMenu
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonRotateCellLeft
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonRotateCellRight
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonSwitchScreen
import kotlinx.android.synthetic.main.fragment_hero_screen.cellName
import kotlinx.android.synthetic.main.fragment_hero_screen.editorCanvasView
import kotlinx.android.synthetic.main.fragment_hero_screen.kittaroAvatar
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonAttackHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonEnergyHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonLifeHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonRemoveHex
import kotlinx.android.synthetic.main.fragment_hero_screen.recyclerViewCellLogicPicker
import kotlinx.android.synthetic.main.fragment_hero_screen.recyclerViewConditionsList
import kotlinx.android.synthetic.main.fragment_hero_screen.recyclerViewRulesList
import kotlinx.android.synthetic.main.fragment_hero_screen.textHeroHistory
import kotlinx.android.synthetic.main.fragment_hero_screen.textMoney
import kotlinx.android.synthetic.main.fragment_hero_screen.zoiAvatar
import javax.inject.Inject
import kotlin.math.PI

class HeroScreenFragment: Fragment(), HeroScreen.View {

    @Inject lateinit var presenter: HeroScreen.Presenter
    @Inject lateinit var hexMath: HexMath
    @Inject lateinit var drawUtils: DrawUtils
    @Inject lateinit var resourceProvider: Common.ResourceProvider

    private val disposables = CompositeDisposable()
    private var moneyDisposable: Disposable? = null
    private var isCellLogicViewsShown = false

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hero_screen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()
        initiateCanvasView()

        buttonMainMenu.setOnClickListener { presenter.openMainMenu() }
        radioButtonLifeHex.setOnClickListener { onHexTypeRadioButtonClicked(it) }
        radioButtonAttackHex.setOnClickListener { onHexTypeRadioButtonClicked(it) }
        radioButtonEnergyHex.setOnClickListener { onHexTypeRadioButtonClicked(it) }
        radioButtonRemoveHex.setOnClickListener { onHexTypeRadioButtonClicked(it) }
        buttonRotateCellLeft.setOnClickListener { onCellRotateButtonClicked(it) }
        buttonRotateCellRight.setOnClickListener { onCellRotateButtonClicked(it) }
        buttonSwitchScreen.setOnClickListener { switchCellLogicEditorViews() }
        buttonIncreaseRulePriority.setOnClickListener { presenter.increaseSelectedRulePriority() }
        buttonDecreaseRulePriority.setOnClickListener { presenter.decreaseSelectedRulePriority() }

        kittaroAvatar.setOnClickListener { presenter.initialize(KITTARO_INDEX) }
        zoiAvatar.setOnClickListener { presenter.initialize(ZOI_INDEX) }
        aimaAvatar.setOnClickListener { presenter.initialize(AIMA_INDEX) }

        recyclerViewRulesList.layoutManager = LinearLayoutManager(context)
        recyclerViewRulesList.adapter = RulesRecyclerViewAdapter(presenter, resourceProvider)

        recyclerViewConditionsList.layoutManager = LinearLayoutManager(context)
        recyclerViewConditionsList.adapter = ConditionsRecyclerViewAdapter(presenter, resourceProvider)

        recyclerViewCellLogicPicker.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCellLogicPicker.adapter = PickerRecyclerViewAdapter(presenter)

        disposables.addAll(
                presenter.getCellProvider().subscribe {
                    editorCanvasView.cell = it
                    textMoney.text = resources.getString(R.string.text_money, it.data.money)
                    highlightTips(editorCanvasView.selectedCellType)

                    /// TODO: make it cleaner
                    moneyDisposable?.dispose()
                    moneyDisposable = it.getMoneyProvider().subscribe { money ->
                        textMoney.text = resources.getString(R.string.text_money, money)
                    }
                },
                presenter.getBackgroundCellRadiusProvider().subscribe {
                    editorCanvasView.backgroundFieldRadius = it
                    editorCanvasView.invalidate()
                },
                presenter.rulesUpdateNotifier().subscribe {
                    recyclerViewRulesList.adapter.notifyDataSetChanged()
                },
                presenter.conditionsUpdateNotifier().subscribe {
                    recyclerViewConditionsList.adapter.notifyDataSetChanged()
                },
                presenter.optionsUpdateNotifier().subscribe {
                    recyclerViewCellLogicPicker.adapter.notifyDataSetChanged()
                }
        )
        presenter.initialize(KITTARO_INDEX)
    }

    override fun onDestroyView() {
        disposables.dispose()
        moneyDisposable?.dispose()
        super.onDestroyView()
    }
    //endregion

    //region Editor.View methods
    override fun highlightTips(type: Hex.Type) {
        editorCanvasView.tipHexes = presenter.getTipHexes(type)
        editorCanvasView.invalidate()
    }
    //endregion

    //region HeroScreen.View methods
    override fun setPickerBackground(colorId: Int) {
        recyclerViewCellLogicPicker.setBackgroundColor(ContextCompat.getColor(requireContext(), colorId))
    }

    override fun setCellName(name: String) {
        cellName.text = name
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

    private fun onHexTypeRadioButtonClicked(view: View) {
        val type = when (view.id) {
            R.id.radioButtonLifeHex -> Hex.Type.LIFE
            R.id.radioButtonEnergyHex -> Hex.Type.ENERGY
            R.id.radioButtonAttackHex -> Hex.Type.ATTACK
            else -> Hex.Type.REMOVE
        }
        editorCanvasView.selectedCellType = type
        editorCanvasView.tipHexes = presenter.getTipHexes(type)
        editorCanvasView.invalidate()
    }

    private fun onCellRotateButtonClicked(view: View) {
        editorCanvasView.tipHexes = null
        when (view.id) {
            R.id.buttonRotateCellLeft -> animateCellRotationLeft()
            R.id.buttonRotateCellRight -> animateCellRotationRight()
        }
    }

    private fun animateCellRotationLeft() {
        ValueAnimator.ofFloat(0f, -PI.toFloat() / 3f).apply {
            duration = 1000
            addUpdateListener {
                editorCanvasView.cell?.animationData?.rotation = it.animatedValue as Float
                editorCanvasView.invalidate()
            }
            addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    editorCanvasView.cell?.animationData?.rotation = 0f
                    presenter.rotateCellLeft()
                    editorCanvasView.tipHexes = presenter.getTipHexes(editorCanvasView.selectedCellType)
                    editorCanvasView.invalidate()
                }
            })
            start()
        }
    }

    private fun animateCellRotationRight() {
        ValueAnimator.ofFloat(0f, PI.toFloat() / 3f).apply {
            duration = 1000
            addUpdateListener {
                editorCanvasView.cell?.animationData?.rotation = it.animatedValue as Float
                editorCanvasView.invalidate()
            }
            addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    editorCanvasView.cell?.animationData?.rotation = 0f
                    presenter.rotateCellRight()
                    editorCanvasView.tipHexes = presenter.getTipHexes(editorCanvasView.selectedCellType)
                    editorCanvasView.invalidate()
                }
            })
            start()
        }
    }

    private fun switchCellLogicEditorViews() {
        isCellLogicViewsShown = !isCellLogicViewsShown

        val cellLogicVisibility = getVisibilityByBool(isCellLogicViewsShown)
        val editorVisibility = getVisibilityByBool(!isCellLogicViewsShown)

        /// TODO: energy hex is available not from the beginning
        for (view in arrayListOf<View>(radioButtonLifeHex, radioButtonAttackHex, radioButtonEnergyHex,
                                       radioButtonRemoveHex, textMoney, editorCanvasView, textHeroHistory,
                                       buttonRotateCellLeft, buttonRotateCellRight))
            view.visibility = editorVisibility

        for (view in arrayListOf<View>(recyclerViewRulesList, recyclerViewConditionsList, recyclerViewCellLogicPicker,
                                       buttonIncreaseRulePriority, buttonDecreaseRulePriority))
            view.visibility = cellLogicVisibility
    }

    private fun getVisibilityByBool(b: Boolean): Int = if (b) View.VISIBLE else View.GONE
    //endregion

    companion object {
        private const val TAG = "HeroScreenFragment"

        private const val KITTARO_INDEX = 0
        private const val ZOI_INDEX = 1
        private const val AIMA_INDEX = 2

        @JvmStatic
        fun newInstance(bundle: Bundle?): HeroScreenFragment {
            val fragment = HeroScreenFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
