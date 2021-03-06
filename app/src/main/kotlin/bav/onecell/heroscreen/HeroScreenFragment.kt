package bav.onecell.heroscreen

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.DragEvent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.celllogic.conditions.ConditionsRecyclerViewAdapter
import bav.onecell.celllogic.rules.RulesRecyclerViewAdapter
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.extensions.visible
import bav.onecell.common.view.DrawUtils
import bav.onecell.common.view.HexPicker
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonAddNewCondition
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonAddNewRule
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonClearCell
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonNextScene
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonRotateCellLeft
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonRotateCellRight
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonSwitchScreen
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonTransformAttackToLifeHex
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonTransformDeathRayToLifeHex
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonTransformEnergyToLifeHex
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonTransformHexes
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonTransformLifeToAttackHex
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonTransformLifeToDeathRayHex
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonTransformLifeToEnergyHex
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonTransformLifeToOmniBulletHex
import kotlinx.android.synthetic.main.fragment_hero_screen.buttonTransformOmniBulletToLifeHex
import kotlinx.android.synthetic.main.fragment_hero_screen.cellName
import kotlinx.android.synthetic.main.fragment_hero_screen.editorCanvasView
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonAttackHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonDeathRayHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonEnergyHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonLifeHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonOmniBulletHex
import kotlinx.android.synthetic.main.fragment_hero_screen.radioButtonRemoveHex
import kotlinx.android.synthetic.main.fragment_hero_screen.recyclerViewAvatars
import kotlinx.android.synthetic.main.fragment_hero_screen.recyclerViewConditionsList
import kotlinx.android.synthetic.main.fragment_hero_screen.recyclerViewRulesList
import kotlinx.android.synthetic.main.fragment_hero_screen.textHeroHistory
import kotlinx.android.synthetic.main.view_hex_picker.view.buttonHex
import kotlinx.android.synthetic.main.view_hex_picker.view.selection
import kotlinx.android.synthetic.main.view_hex_picker.view.textViewHexCount
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_hero_screen.newCharacterAvatar

class HeroScreenFragment: Fragment(), HeroScreen.View {

    @Inject lateinit var presenter: HeroScreen.Presenter
    @Inject lateinit var hexMath: HexMath
    @Inject lateinit var drawUtils: DrawUtils
    @Inject lateinit var resourceProvider: Common.ResourceProvider
    @Inject lateinit var gameState: Common.GameState
    @Inject lateinit var analytics: Common.Analytics
    @Inject lateinit var objectPool: Common.ObjectPool

    private val disposables = CompositeDisposable()
    private var isCellLogicViewsShown = false
    private var isHexesTransformShown = false
    private var nextScene: Int = 0

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hero_screen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        nextScene = resourceProvider.getIdIdentifier(getString(arguments!!.getInt(Consts.NEXT_SCENE)))
        initiateCanvasView()
        initiateButtons()

        recyclerViewAvatars.layoutManager = LinearLayoutManager(context)
        recyclerViewAvatars.addItemDecoration(HeroIconsRecyclerViewAdapter.VerticalSpaceItemDecoration())
        recyclerViewAvatars.adapter = HeroIconsRecyclerViewAdapter(presenter, resourceProvider)
        checkLastCellsCount()

        recyclerViewRulesList.layoutManager = LinearLayoutManager(context)
        val rulesAdapter = RulesRecyclerViewAdapter(presenter, resourceProvider)
        recyclerViewRulesList.adapter = rulesAdapter
        val ruleTouchHelperCallback = RulesRecyclerViewAdapter.SimpleItemTouchHelperCallback(rulesAdapter)
        val ruleTouchHelper = ItemTouchHelper(ruleTouchHelperCallback)
        ruleTouchHelper.attachToRecyclerView(recyclerViewRulesList)

        recyclerViewConditionsList.layoutManager = LinearLayoutManager(context)
        val conditionsAdapter = ConditionsRecyclerViewAdapter(presenter, resourceProvider)
        recyclerViewConditionsList.adapter = conditionsAdapter
        val conditionTouchHelperCallback = ConditionsRecyclerViewAdapter.SimpleItemTouchHelperCallback(conditionsAdapter)
        val conditionTouchHelper = ItemTouchHelper(conditionTouchHelperCallback)
        conditionTouchHelper.attachToRecyclerView(recyclerViewConditionsList)

        /*cellName.setOnClickListener {
            textHeroHistory.visible = !textHeroHistory.visible
        }*/

        disposables.addAll(
                presenter.getCellProvider().subscribe {
                    it.evaluateCellHexesPower()
                    editorCanvasView.cell = it
                    updateCellRepresentation()
                    highlightTips(editorCanvasView.selectedCellType)
                    setNextSceneButtonVisibility(it.data.hexes.isNotEmpty())
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
                }
        )
        presenter.initialize(Consts.KITTARO_INDEX)
    }

    override fun onPause() {
        gameState.setLastNavDestinationId(findNavController().currentDestination?.id ?: 0)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        analytics.setCurrentScreen(requireActivity(), SCREEN_NAME, this::class.qualifiedName)
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

    override fun updateCellRepresentation() {
        editorCanvasView.updateCellRepresentation()
    }
    //endregion

    //region HeroScreen.View methods
    override fun setCellName(name: String) {
        cellName.text = name
    }

    override fun updateAvatars() {
        recyclerViewAvatars.adapter?.notifyDataSetChanged()
    }

    override fun updateHexesInBucket(type: Hex.Type, count: Int) {
        val hexPicker = when (type) {
            Hex.Type.LIFE -> radioButtonLifeHex
            Hex.Type.ENERGY -> radioButtonEnergyHex
            Hex.Type.ATTACK -> radioButtonAttackHex
            Hex.Type.DEATH_RAY -> radioButtonDeathRayHex
            Hex.Type.OMNI_BULLET -> radioButtonOmniBulletHex
            else -> radioButtonRemoveHex
        }
        hexPicker.setHexCount(count)
    }

    override fun setNextSceneButtonVisibility(visible: Boolean) {
        buttonNextScene.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }
    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(HeroScreenModule(this))
                .inject(this)
    }

    private fun checkLastCellsCount() {
        if (presenter.getCellCount() > lastCellsCount) {
            val newCharacterIndex = lastCellsCount
            lastCellsCount = presenter.getCellCount()
            newCharacterAvatar.setImageResource(
                    when (newCharacterIndex) {
                        Consts.ZOI_INDEX -> R.drawable.ic_avatar_zoi
                        Consts.AIMA_INDEX -> R.drawable.ic_avatar_aima
                        else -> R.drawable.ic_hex_picker_selection
                    }
            )
            AnimationUtils.loadAnimation(context, R.anim.new_character_appears).also { newCharacterAppearsAnimation ->
                newCharacterAvatar.startAnimation(newCharacterAppearsAnimation)
            }
        }
    }

    private fun initiateButtons() {
        buttonNextScene.setOnClickListener { view ->
            if (!presenter.isThereAnyEmptyCell()) {
                view.findNavController().navigate(nextScene)
                presenter.openMainMenu()
            }
            else {
                Toast.makeText(context, R.string.empty_cell_toast_text, Toast.LENGTH_SHORT).show()
            }
        }

        initiateHexesButtons()

        buttonRotateCellLeft.setOnClickListener { onCellRotateButtonClicked(it) }
        buttonRotateCellRight.setOnClickListener { onCellRotateButtonClicked(it) }

        buttonSwitchScreen.visible = gameState.isDecisionPositive(Common.GameState.BATTLE_LOGIC_AVAILABLE)
        buttonSwitchScreen.setOnClickListener { switchCellLogicEditorViews() }

        buttonAddNewCondition.setOnClickListener {
            if (presenter.createNewCondition())
                showConditionCreationPopupMenu(it)
        }
        buttonAddNewRule.setOnClickListener { presenter.createNewRule() }
        buttonClearCell.setOnClickListener { presenter.clearCellHexes() }

        initiateTransformHexesButtons()
    }

    private fun showConditionCreationPopupMenu(view: View) {
        val popupMenu = PopupMenu(view.context, view, Gravity.TOP)
        forceIconsShow(popupMenu)
        popupMenu.inflate(R.menu.condition_creation)
        popupMenu.setOnMenuItemClickListener(conditionCreationMenuItemClickListener)
        popupMenu.show()
    }

    private val conditionCreationMenuItemClickListener = PopupMenu.OnMenuItemClickListener {
        when (it.groupId) {
            R.id.group_field_to_check -> presenter.setFieldToCheckForCurrentCondition(it.itemId)
            R.id.group_operation -> presenter.setOperationForCurrentCondition(it.itemId)
            R.id.group_expected_value -> presenter.setExpectedValueForCurrentCondition(it.itemId)
        }
        true
    }

    // From here: https://readyandroid.wordpress.com/popup-menu-with-icon/
    private fun forceIconsShow(popup: PopupMenu) {
        try {
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popup)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon",
                                                                   Boolean::class.javaPrimitiveType)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initiateHexesButtons() {
        for (view in arrayListOf<HexPicker>(radioButtonLifeHex, radioButtonAttackHex, radioButtonEnergyHex,
                                            radioButtonDeathRayHex, radioButtonOmniBulletHex, radioButtonRemoveHex)) {
            view.setButtonClickListener { onHexTypeButtonClicked(it) }
            view.setButtonLongClickListener { onHexTypeButtonLongClicked(it) }
            view.selection.visibility = View.INVISIBLE
        }

        radioButtonLifeHex.buttonHex.setImageResource(R.drawable.ic_hex_life)
        radioButtonAttackHex.buttonHex.setImageResource(R.drawable.ic_hex_attack)
        radioButtonEnergyHex.buttonHex.setImageResource(R.drawable.ic_hex_energy)
        radioButtonDeathRayHex.buttonHex.setImageResource(R.drawable.ic_hex_death_ray)
        radioButtonOmniBulletHex.buttonHex.setImageResource(R.drawable.ic_hex_omni_bullet)

        radioButtonLifeHex.selection.visibility = View.VISIBLE

        radioButtonRemoveHex.buttonHex.setImageResource(R.drawable.ic_remove_icon)
        radioButtonRemoveHex.textViewHexCount.visible = false

        setHexButtonsVisibilityBasedOnGameState()
    }

    private fun setHexButtonsVisibilityBasedOnGameState() {
        radioButtonAttackHex.visible = gameState.isDecisionPositive(Common.GameState.ATTACK_HEXES_AVAILABLE)
        radioButtonEnergyHex.visible = gameState.isDecisionPositive(Common.GameState.ENERGY_HEXES_AVAILABLE)
        radioButtonDeathRayHex.visible = gameState.isDecisionPositive(Common.GameState.DEATH_RAY_HEXES_AVAILABLE)
        radioButtonOmniBulletHex.visible = gameState.isDecisionPositive(Common.GameState.OMNI_BULLET_HEXES_AVAILABLE)
    }

    private fun initiateTransformHexesButtons() {
        buttonTransformHexes.setOnClickListener { switchHexesTransformViews() }
        buttonTransformLifeToAttackHex.setOnClickListener { presenter.transformLifeHexToAttack() }
        buttonTransformLifeToEnergyHex.setOnClickListener { presenter.transformLifeHexToEnergy() }
        buttonTransformLifeToDeathRayHex.setOnClickListener { presenter.transformLifeHexToDeathRay() }
        buttonTransformLifeToOmniBulletHex.setOnClickListener { presenter.transformLifeHexToOmniBullet() }
        buttonTransformAttackToLifeHex.setOnClickListener { presenter.transformAttackHexToLife() }
        buttonTransformEnergyToLifeHex.setOnClickListener { presenter.transformEnergyHexToLife() }
        buttonTransformDeathRayToLifeHex.setOnClickListener { presenter.transformDeathRayHexToLife() }
        buttonTransformOmniBulletToLifeHex.setOnClickListener { presenter.transformOmniBulletHexToLife() }

        setTransformHexesButtonsVisibilityBasedOnGameState()
    }

    private fun setTransformHexesButtonsVisibilityBasedOnGameState() {
        buttonTransformHexes.visible = gameState.isDecisionPositive(Common.GameState.HEX_TRANSFORMATION_AVAILABLE)
        val buttons = arrayListOf<View>(buttonTransformAttackToLifeHex, buttonTransformDeathRayToLifeHex,
                                        buttonTransformEnergyToLifeHex, buttonTransformLifeToAttackHex,
                                        buttonTransformLifeToDeathRayHex, buttonTransformLifeToEnergyHex,
                                        buttonTransformOmniBulletToLifeHex, buttonTransformLifeToOmniBulletHex)
        if (!buttonTransformHexes.visible) {
            for (view in buttons) view.visible = false
        }
        else if (isHexesTransformShown) {
            buttonTransformAttackToLifeHex.visible = gameState.isDecisionPositive(Common.GameState.ATTACK_HEXES_AVAILABLE)
            buttonTransformLifeToAttackHex.visible = gameState.isDecisionPositive(Common.GameState.ATTACK_HEXES_AVAILABLE)

            buttonTransformLifeToEnergyHex.visible = gameState.isDecisionPositive(Common.GameState.ENERGY_HEXES_AVAILABLE)
            buttonTransformEnergyToLifeHex.visible = gameState.isDecisionPositive(Common.GameState.ENERGY_HEXES_AVAILABLE)

            buttonTransformLifeToDeathRayHex.visible = gameState.isDecisionPositive(Common.GameState.DEATH_RAY_HEXES_AVAILABLE)
            buttonTransformDeathRayToLifeHex.visible = gameState.isDecisionPositive(Common.GameState.DEATH_RAY_HEXES_AVAILABLE)

            buttonTransformLifeToOmniBulletHex.visible = gameState.isDecisionPositive(Common.GameState.OMNI_BULLET_HEXES_AVAILABLE)
            buttonTransformOmniBulletToLifeHex.visible = gameState.isDecisionPositive(Common.GameState.OMNI_BULLET_HEXES_AVAILABLE)
        }
    }

    private fun initiateCanvasView() {
        editorCanvasView.inject(hexMath, drawUtils)
        editorCanvasView.presenter = presenter
        editorCanvasView.setOnDragListener(mDragListen)
        editorCanvasView.objectPool = objectPool
    }

    private fun getHexTypeBasedOnHexButtonId(id: Int): Hex.Type = when (id) {
        R.id.radioButtonLifeHex -> Hex.Type.LIFE
        R.id.radioButtonEnergyHex -> Hex.Type.ENERGY
        R.id.radioButtonAttackHex -> Hex.Type.ATTACK
        R.id.radioButtonDeathRayHex -> Hex.Type.DEATH_RAY
        R.id.radioButtonOmniBulletHex -> Hex.Type.OMNI_BULLET
        else -> Hex.Type.REMOVE
    }

    private fun onHexTypeButtonClicked(view: View) {
        for (picker in arrayListOf<HexPicker>(radioButtonLifeHex, radioButtonAttackHex, radioButtonEnergyHex,
                                            radioButtonDeathRayHex, radioButtonOmniBulletHex, radioButtonRemoveHex)) {
            picker.selection.visibility = View.INVISIBLE
        }
        view.selection.visibility = View.VISIBLE
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
        highlightTips(type)
    }

    private val draggedHex = Hex()
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

                editorCanvasView.pointToHex(event.x, event.y, draggedHex)
                val typeOrdinal = item.text.toString().toInt()
                if (typeOrdinal == Hex.Type.REMOVE.ordinal) {
                    presenter.removeHexFromCell(draggedHex)
                } else {
                    draggedHex.type = Hex.Type.values()[typeOrdinal]
                    presenter.addHexToCell(draggedHex)
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
        /// TODO: reuse tipHexes instead of recreate them
        editorCanvasView.tipHexes = null
        when (view.id) {
            R.id.buttonRotateCellLeft -> {
                presenter.rotateCellLeft()
                editorCanvasView.tipHexes = presenter.getTipHexes(editorCanvasView.selectedCellType)
                editorCanvasView.invalidate()
            }
            R.id.buttonRotateCellRight -> {
                presenter.rotateCellRight()
                editorCanvasView.tipHexes = presenter.getTipHexes(editorCanvasView.selectedCellType)
                editorCanvasView.invalidate()
            }
        }
    }

    private fun switchCellLogicEditorViews() {
        isCellLogicViewsShown = !isCellLogicViewsShown

        val cellLogicVisibility = isCellLogicViewsShown
        val editorVisibility = !isCellLogicViewsShown

        buttonSwitchScreen.setImageDrawable(
                ContextCompat.getDrawable(requireContext(),
                                          if (!isCellLogicViewsShown) R.drawable.ic_button_brain
                                          else R.drawable.ic_button_editor)
        )

        /// TODO: energy hex is available not from the beginning
        for (view in arrayListOf<View>(radioButtonLifeHex, radioButtonAttackHex, radioButtonEnergyHex,
                                       radioButtonRemoveHex, editorCanvasView,
                                       buttonRotateCellLeft, buttonRotateCellRight, radioButtonDeathRayHex,
                                       radioButtonOmniBulletHex,
                                       buttonTransformHexes, buttonTransformAttackToLifeHex, buttonTransformDeathRayToLifeHex,
                                       buttonTransformOmniBulletToLifeHex, buttonTransformLifeToOmniBulletHex,
                                       buttonTransformEnergyToLifeHex, buttonTransformLifeToAttackHex,
                                       buttonTransformLifeToDeathRayHex, buttonTransformLifeToEnergyHex,
                                       buttonClearCell)) {
            view.visible = editorVisibility
        }

        textHeroHistory.visible = !cellLogicVisibility and textHeroHistory.visible

        for (view in arrayListOf<View>(recyclerViewRulesList, recyclerViewConditionsList,
                                       buttonAddNewRule, buttonAddNewCondition))
            view.visible = cellLogicVisibility

        if (editorVisibility) {
            setTransformHexesButtonsVisibilityBasedOnGameState()
            setHexButtonsVisibilityBasedOnGameState()
            showHexesTransformOrEditorView()
        }
    }

    private fun switchHexesTransformViews() {
        isHexesTransformShown = !isHexesTransformShown
        showHexesTransformOrEditorView()
    }

    private fun showHexesTransformOrEditorView() {
        for (view in arrayListOf<View>(editorCanvasView, buttonRotateCellLeft, buttonRotateCellRight))
            view.visible = !isHexesTransformShown

        for (view in arrayListOf<View>(buttonTransformAttackToLifeHex, buttonTransformDeathRayToLifeHex,
                                       buttonTransformEnergyToLifeHex, buttonTransformLifeToAttackHex,
                                       buttonTransformLifeToDeathRayHex, buttonTransformLifeToEnergyHex,
                                       buttonTransformOmniBulletToLifeHex, buttonTransformLifeToOmniBulletHex))
            view.visible = isHexesTransformShown

        if (isHexesTransformShown)
            setTransformHexesButtonsVisibilityBasedOnGameState()
    }
    //endregion

    companion object {
        private const val TAG = "HeroScreenFragment"
        private const val SCREEN_NAME = "Heroes screen"

        private var lastCellsCount = 1
    }
}
