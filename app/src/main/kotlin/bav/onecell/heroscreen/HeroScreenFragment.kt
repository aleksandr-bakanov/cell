package bav.onecell.heroscreen

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.celllogic.conditions.ConditionsRecyclerViewAdapter
import bav.onecell.celllogic.picker.PickerRecyclerViewAdapter
import bav.onecell.celllogic.rules.RulesRecyclerViewAdapter
import bav.onecell.common.Common
import bav.onecell.common.view.DrawUtils
import bav.onecell.common.view.HexPicker
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonDecreaseRulePriority
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonIncreaseRulePriority
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonMainMenu
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonRotateCellLeft
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonRotateCellRight
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonSwitchScreen
import kotlinx.android.synthetic.main.fragment_hero_screen.cellName
import kotlinx.android.synthetic.main.fragment_hero_screen.editorCanvasView
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonAttackHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonDeathRayHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonEnergyHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonLifeHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonRemoveHex
import kotlinx.android.synthetic.main.fragment_hero_screen.recyclerViewAvatars
import kotlinx.android.synthetic.main.fragment_hero_screen.recyclerViewCellLogicPicker
import kotlinx.android.synthetic.main.fragment_hero_screen.recyclerViewConditionsList
import kotlinx.android.synthetic.main.fragment_hero_screen.recyclerViewRulesList
import kotlinx.android.synthetic.main.fragment_hero_screen.textHeroHistory
import kotlinx.android.synthetic.main.view_hex_picker.view.buttonHex
import javax.inject.Inject
import kotlin.math.PI

class HeroScreenFragment: Fragment(), HeroScreen.View {

    @Inject lateinit var presenter: HeroScreen.Presenter
    @Inject lateinit var hexMath: HexMath
    @Inject lateinit var drawUtils: DrawUtils
    @Inject lateinit var resourceProvider: Common.ResourceProvider

    private val disposables = CompositeDisposable()
    private var isCellLogicViewsShown = false

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hero_screen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        initiateCanvasView()
        initiateButtons()

        recyclerViewAvatars.layoutManager = LinearLayoutManager(context)
        recyclerViewAvatars.addItemDecoration(HeroIconsRecyclerViewAdapter.VerticalSpaceItemDecoration())
        recyclerViewAvatars.adapter = HeroIconsRecyclerViewAdapter(presenter, resourceProvider)

        recyclerViewRulesList.layoutManager = LinearLayoutManager(context)
        recyclerViewRulesList.adapter = RulesRecyclerViewAdapter(presenter, resourceProvider)

        recyclerViewConditionsList.layoutManager = LinearLayoutManager(context)
        recyclerViewConditionsList.adapter = ConditionsRecyclerViewAdapter(presenter, resourceProvider)

        recyclerViewCellLogicPicker.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCellLogicPicker.adapter = PickerRecyclerViewAdapter(presenter)

        disposables.addAll(
                presenter.getCellProvider().subscribe {
                    editorCanvasView.cell = it
                    highlightTips(editorCanvasView.selectedCellType)
                },
                presenter.getBackgroundCellRadiusProvider().subscribe {
                    editorCanvasView.backgroundFieldRadius = it
                    editorCanvasView.invalidate()
                },
                presenter.rulesUpdateNotifier().subscribe {
                    recyclerViewRulesList.adapter?.notifyDataSetChanged()
                },
                presenter.conditionsUpdateNotifier().subscribe {
                    recyclerViewConditionsList.adapter?.notifyDataSetChanged()
                },
                presenter.optionsUpdateNotifier().subscribe {
                    recyclerViewCellLogicPicker.adapter?.notifyDataSetChanged()
                }
        )
        presenter.initialize(KITTARO_INDEX)
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }
    //endregion

    //region Editor.View methods
    override fun highlightTips(type: Hex.Type) {
        editorCanvasView.tipHexes = presenter.getTipHexes(type)
        editorCanvasView.invalidate()
    }

    override fun updateHexesInBucket(type: Hex.Type, count: Int) {
        val hexPicker = when (type) {
            Hex.Type.LIFE -> radioButtonLifeHex
            Hex.Type.ENERGY -> radioButtonEnergyHex
            Hex.Type.ATTACK -> radioButtonAttackHex
            Hex.Type.DEATH_RAY -> radioButtonDeathRayHex
            else -> radioButtonRemoveHex
        }
        hexPicker.setHexCount(count)
    }
    //endregion

    //region HeroScreen.View methods
    override fun setPickerBackground(colorId: Int) {
        recyclerViewCellLogicPicker.setBackgroundColor(ContextCompat.getColor(requireContext(), colorId))
    }

    override fun setCellName(name: String) {
        cellName.text = name
    }

    override fun updateAvatars() {
        recyclerViewAvatars.adapter?.notifyDataSetChanged()
    }
    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(HeroScreenModule(this))
                .inject(this)
    }

    private fun initiateButtons() {
        buttonMainMenu.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_heroScreenFragment_to_mainFragment)
            presenter.openMainMenu()
        }

        for (view in arrayListOf<HexPicker>(radioButtonLifeHex, radioButtonAttackHex, radioButtonEnergyHex,
                                            radioButtonDeathRayHex, radioButtonRemoveHex)) {
            view.setButtonClickListener { onHexTypeButtonClicked(it) }
            view.setButtonLongClickListener { onHexTypeButtonLongClicked(it) }
        }

        radioButtonLifeHex.buttonHex.setImageResource(R.drawable.ic_hex_life)
        radioButtonAttackHex.buttonHex.setImageResource(R.drawable.ic_hex_attack)
        radioButtonEnergyHex.buttonHex.setImageResource(R.drawable.ic_hex_energy)
        radioButtonDeathRayHex.buttonHex.setImageResource(R.drawable.ic_hex_death_ray)
        radioButtonRemoveHex.buttonHex.setImageResource(R.drawable.ic_hex_life)

        buttonRotateCellLeft.setOnClickListener { onCellRotateButtonClicked(it) }
        buttonRotateCellRight.setOnClickListener { onCellRotateButtonClicked(it) }
        buttonSwitchScreen.setOnClickListener { switchCellLogicEditorViews() }
        buttonIncreaseRulePriority.setOnClickListener { presenter.increaseSelectedRulePriority() }
        buttonDecreaseRulePriority.setOnClickListener { presenter.decreaseSelectedRulePriority() }
    }

    private fun initiateCanvasView() {
        editorCanvasView.hexMath = hexMath
        editorCanvasView.drawUtils = drawUtils
        editorCanvasView.presenter = presenter
        editorCanvasView.setOnDragListener(mDragListen)
    }

    private fun getHexTypeBasedOnHexButtonId(id: Int): Hex.Type = when (id) {
        R.id.radioButtonLifeHex -> Hex.Type.LIFE
        R.id.radioButtonEnergyHex -> Hex.Type.ENERGY
        R.id.radioButtonAttackHex -> Hex.Type.ATTACK
        R.id.radioButtonDeathRayHex -> Hex.Type.DEATH_RAY
        else -> Hex.Type.REMOVE
    }

    private fun onHexTypeButtonClicked(view: View) {
        highlightEditorTips(getHexTypeBasedOnHexButtonId(view.id))
    }

    private fun onHexTypeButtonLongClicked(view: View) {
        val type = getHexTypeBasedOnHexButtonId(view.id)
        highlightEditorTips(type)
        val typeOrdinal = type.ordinal
        val item = ClipData.Item(typeOrdinal.toString())
        val dragData = ClipData(
                typeOrdinal.toString(),
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                item)

        val shadow = View.DragShadowBuilder(view.buttonHex)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            view.startDragAndDrop(dragData, shadow, null, 0)
        }
        else {
            view.startDrag(dragData, shadow, null, 0)
        }
    }

    private fun highlightEditorTips(type: Hex.Type) {
        editorCanvasView.selectedCellType = type
        editorCanvasView.tipHexes = presenter.getTipHexes(type)
        editorCanvasView.invalidate()
    }

    private val mDragListen = View.OnDragListener { v, event ->
        // Handles each of the expected events
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                // Determines if this View can accept the dragged data
                event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED,
            DragEvent.ACTION_DRAG_LOCATION,
            DragEvent.ACTION_DRAG_EXITED,
            DragEvent.ACTION_DRAG_ENDED -> {
                true
            }
            DragEvent.ACTION_DROP -> {
                // Gets the item containing the dragged data
                val item: ClipData.Item = event.clipData.getItemAt(0)

                val hex = editorCanvasView.pointToHex(event.x, event.y)
                val typeOrdinal = item.text.toString().toInt()
                if (typeOrdinal == Hex.Type.REMOVE.ordinal) {
                    presenter.removeHexFromCell(hex)
                } else {
                    hex.type = Hex.Type.values()[typeOrdinal]
                    presenter.addHexToCell(hex)
                }
                v.invalidate()

                // Returns true. DragEvent.getResult() will return true.
                true
            }
            else -> {
                // An unknown action type was received.
                false
            }
        }
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
                                       radioButtonRemoveHex, editorCanvasView, textHeroHistory,
                                       buttonRotateCellLeft, buttonRotateCellRight, radioButtonDeathRayHex))
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
