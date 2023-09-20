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
import bav.onecell.databinding.FragmentHeroScreenBinding
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

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

    private var _binding: FragmentHeroScreenBinding? = null
    private val binding get() = _binding!!

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHeroScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        nextScene = resourceProvider.getIdIdentifier(getString(requireArguments().getInt(Consts.NEXT_SCENE)))
        initiateCanvasView()
        initiateButtons()

        binding.recyclerViewAvatars.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAvatars.addItemDecoration(HeroIconsRecyclerViewAdapter.VerticalSpaceItemDecoration())
        binding.recyclerViewAvatars.adapter = HeroIconsRecyclerViewAdapter(presenter, resourceProvider)
        checkLastCellsCount()

        binding.recyclerViewRulesList.layoutManager = LinearLayoutManager(context)
        val rulesAdapter = RulesRecyclerViewAdapter(presenter, resourceProvider)
        binding.recyclerViewRulesList.adapter = rulesAdapter
        val ruleTouchHelperCallback = RulesRecyclerViewAdapter.SimpleItemTouchHelperCallback(rulesAdapter)
        val ruleTouchHelper = ItemTouchHelper(ruleTouchHelperCallback)
        ruleTouchHelper.attachToRecyclerView(binding.recyclerViewRulesList)

        binding.recyclerViewConditionsList.layoutManager = LinearLayoutManager(context)
        val conditionsAdapter = ConditionsRecyclerViewAdapter(presenter, resourceProvider)
        binding.recyclerViewConditionsList.adapter = conditionsAdapter
        val conditionTouchHelperCallback = ConditionsRecyclerViewAdapter.SimpleItemTouchHelperCallback(conditionsAdapter)
        val conditionTouchHelper = ItemTouchHelper(conditionTouchHelperCallback)
        conditionTouchHelper.attachToRecyclerView(binding.recyclerViewConditionsList)

        /*cellName.setOnClickListener {
            textHeroHistory.visible = !textHeroHistory.visible
        }*/

        disposables.addAll(
                presenter.getCellProvider().subscribe {
                    it.evaluateCellHexesPower()
                    binding.editorCanvasView.cell = it
                    updateCellRepresentation()
                    highlightTips(binding.editorCanvasView.selectedCellType)
                    setNextSceneButtonVisibility(it.data.hexes.isNotEmpty())
                },
                presenter.getBackgroundCellRadiusProvider().subscribe {
                    binding.editorCanvasView.backgroundFieldRadius = it
                    binding.editorCanvasView.invalidate()
                },
                presenter.rulesUpdateNotifier().subscribe {
                    binding.recyclerViewRulesList.adapter?.notifyDataSetChanged()
                },
                presenter.conditionsUpdateNotifier().subscribe {
                    binding.recyclerViewConditionsList.adapter?.notifyDataSetChanged()
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
        _binding = null
    }
    //endregion

    //region Editor.View methods
    override fun highlightTips(type: Hex.Type) {
        binding.editorCanvasView.tipHexes = presenter.getTipHexes(type)
        binding.editorCanvasView.invalidate()
    }

    override fun updateCellRepresentation() {
        binding.editorCanvasView.updateCellRepresentation()
    }
    //endregion

    //region HeroScreen.View methods
    override fun setCellName(name: String) {
        binding.cellName.text = name
    }

    override fun updateAvatars() {
        binding.recyclerViewAvatars.adapter?.notifyDataSetChanged()
    }

    override fun updateHexesInBucket(type: Hex.Type, count: Int) {
        val hexPicker = when (type) {
            Hex.Type.LIFE -> binding.radioButtonLifeHex
            Hex.Type.ENERGY -> binding.radioButtonEnergyHex
            Hex.Type.ATTACK -> binding.radioButtonAttackHex
            Hex.Type.DEATH_RAY -> binding.radioButtonDeathRayHex
            Hex.Type.OMNI_BULLET -> binding.radioButtonOmniBulletHex
            else -> binding.radioButtonRemoveHex
        }
        hexPicker.setHexCount(count)
    }

    override fun setNextSceneButtonVisibility(visible: Boolean) {
        binding.buttonNextScene.visibility = if (visible) View.VISIBLE else View.INVISIBLE
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
            binding.newCharacterAvatar.setImageResource(
                    when (newCharacterIndex) {
                        Consts.ZOI_INDEX -> R.drawable.ic_avatar_zoi
                        Consts.AIMA_INDEX -> R.drawable.ic_avatar_aima
                        else -> R.drawable.ic_hex_picker_selection
                    }
            )
            AnimationUtils.loadAnimation(context, R.anim.new_character_appears).also { newCharacterAppearsAnimation ->
                binding.newCharacterAvatar.startAnimation(newCharacterAppearsAnimation)
            }
        }
    }

    private fun initiateButtons() {
        binding.buttonNextScene.setOnClickListener { view ->
            if (!presenter.isThereAnyEmptyCell()) {
                view.findNavController().navigate(nextScene)
                presenter.openMainMenu()
            }
            else {
                Toast.makeText(context, R.string.empty_cell_toast_text, Toast.LENGTH_SHORT).show()
            }
        }

        initiateHexesButtons()

        binding.buttonRotateCellLeft.setOnClickListener { onCellRotateButtonClicked(it) }
        binding.buttonRotateCellRight.setOnClickListener { onCellRotateButtonClicked(it) }

        binding.buttonSwitchScreen.visible = gameState.isDecisionPositive(Common.GameState.BATTLE_LOGIC_AVAILABLE)
        binding.buttonSwitchScreen.setOnClickListener { switchCellLogicEditorViews() }

        binding.buttonAddNewCondition.setOnClickListener {
            if (presenter.createNewCondition())
                showConditionCreationPopupMenu(it)
        }
        binding.buttonAddNewRule.setOnClickListener { presenter.createNewRule() }
        binding.buttonClearCell.setOnClickListener { presenter.clearCellHexes() }

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
        for (view in arrayListOf<HexPicker>(binding.radioButtonLifeHex, binding.radioButtonAttackHex, binding.radioButtonEnergyHex,
                                            binding.radioButtonDeathRayHex, binding.radioButtonOmniBulletHex, binding.radioButtonRemoveHex)) {
            view.setButtonClickListener { onHexTypeButtonClicked(it as HexPicker) }
            view.setButtonLongClickListener { onHexTypeButtonLongClicked(it as HexPicker) }
            view.selection.visibility = View.INVISIBLE
        }

        binding.radioButtonLifeHex.buttonHex.setImageResource(R.drawable.ic_hex_life)
        binding.radioButtonAttackHex.buttonHex.setImageResource(R.drawable.ic_hex_attack)
        binding.radioButtonEnergyHex.buttonHex.setImageResource(R.drawable.ic_hex_energy)
        binding.radioButtonDeathRayHex.buttonHex.setImageResource(R.drawable.ic_hex_death_ray)
        binding.radioButtonOmniBulletHex.buttonHex.setImageResource(R.drawable.ic_hex_omni_bullet)

        binding.radioButtonLifeHex.selection.visibility = View.VISIBLE

        binding.radioButtonRemoveHex.buttonHex.setImageResource(R.drawable.ic_remove_icon)
        binding.radioButtonRemoveHex.textViewHexCount.visible = false

        setHexButtonsVisibilityBasedOnGameState()
    }

    private fun setHexButtonsVisibilityBasedOnGameState() {
        binding.radioButtonAttackHex.visible = gameState.isDecisionPositive(Common.GameState.ATTACK_HEXES_AVAILABLE)
        binding.radioButtonEnergyHex.visible = gameState.isDecisionPositive(Common.GameState.ENERGY_HEXES_AVAILABLE)
        binding.radioButtonDeathRayHex.visible = gameState.isDecisionPositive(Common.GameState.DEATH_RAY_HEXES_AVAILABLE)
        binding.radioButtonOmniBulletHex.visible = gameState.isDecisionPositive(Common.GameState.OMNI_BULLET_HEXES_AVAILABLE)
    }

    private fun initiateTransformHexesButtons() {
        binding.buttonTransformHexes.setOnClickListener { switchHexesTransformViews() }
        binding.buttonTransformLifeToAttackHex.setOnClickListener { presenter.transformLifeHexToAttack() }
        binding.buttonTransformLifeToEnergyHex.setOnClickListener { presenter.transformLifeHexToEnergy() }
        binding.buttonTransformLifeToDeathRayHex.setOnClickListener { presenter.transformLifeHexToDeathRay() }
        binding.buttonTransformLifeToOmniBulletHex.setOnClickListener { presenter.transformLifeHexToOmniBullet() }
        binding.buttonTransformAttackToLifeHex.setOnClickListener { presenter.transformAttackHexToLife() }
        binding.buttonTransformEnergyToLifeHex.setOnClickListener { presenter.transformEnergyHexToLife() }
        binding.buttonTransformDeathRayToLifeHex.setOnClickListener { presenter.transformDeathRayHexToLife() }
        binding.buttonTransformOmniBulletToLifeHex.setOnClickListener { presenter.transformOmniBulletHexToLife() }

        setTransformHexesButtonsVisibilityBasedOnGameState()
    }

    private fun setTransformHexesButtonsVisibilityBasedOnGameState() {
        binding.buttonTransformHexes.visible = gameState.isDecisionPositive(Common.GameState.HEX_TRANSFORMATION_AVAILABLE)
        val buttons = arrayListOf<View>(binding.buttonTransformAttackToLifeHex, binding.buttonTransformDeathRayToLifeHex,
                                        binding.buttonTransformEnergyToLifeHex, binding.buttonTransformLifeToAttackHex,
                                        binding.buttonTransformLifeToDeathRayHex, binding.buttonTransformLifeToEnergyHex,
                                        binding.buttonTransformOmniBulletToLifeHex, binding.buttonTransformLifeToOmniBulletHex)
        if (!binding.buttonTransformHexes.visible) {
            for (view in buttons) view.visible = false
        }
        else if (isHexesTransformShown) {
            binding.buttonTransformAttackToLifeHex.visible = gameState.isDecisionPositive(Common.GameState.ATTACK_HEXES_AVAILABLE)
            binding.buttonTransformLifeToAttackHex.visible = gameState.isDecisionPositive(Common.GameState.ATTACK_HEXES_AVAILABLE)

            binding.buttonTransformLifeToEnergyHex.visible = gameState.isDecisionPositive(Common.GameState.ENERGY_HEXES_AVAILABLE)
            binding.buttonTransformEnergyToLifeHex.visible = gameState.isDecisionPositive(Common.GameState.ENERGY_HEXES_AVAILABLE)

            binding.buttonTransformLifeToDeathRayHex.visible = gameState.isDecisionPositive(Common.GameState.DEATH_RAY_HEXES_AVAILABLE)
            binding.buttonTransformDeathRayToLifeHex.visible = gameState.isDecisionPositive(Common.GameState.DEATH_RAY_HEXES_AVAILABLE)

            binding.buttonTransformLifeToOmniBulletHex.visible = gameState.isDecisionPositive(Common.GameState.OMNI_BULLET_HEXES_AVAILABLE)
            binding.buttonTransformOmniBulletToLifeHex.visible = gameState.isDecisionPositive(Common.GameState.OMNI_BULLET_HEXES_AVAILABLE)
        }
    }

    private fun initiateCanvasView() {
        binding.editorCanvasView.inject(hexMath, drawUtils)
        binding.editorCanvasView.presenter = presenter
        binding.editorCanvasView.setOnDragListener(mDragListen)
        binding.editorCanvasView.objectPool = objectPool
    }

    private fun getHexTypeBasedOnHexButtonId(id: Int): Hex.Type = when (id) {
        R.id.radioButtonLifeHex -> Hex.Type.LIFE
        R.id.radioButtonEnergyHex -> Hex.Type.ENERGY
        R.id.radioButtonAttackHex -> Hex.Type.ATTACK
        R.id.radioButtonDeathRayHex -> Hex.Type.DEATH_RAY
        R.id.radioButtonOmniBulletHex -> Hex.Type.OMNI_BULLET
        else -> Hex.Type.REMOVE
    }

    private fun onHexTypeButtonClicked(view: HexPicker) {
        for (picker in arrayListOf<HexPicker>(binding.radioButtonLifeHex, binding.radioButtonAttackHex, binding.radioButtonEnergyHex,
                                              binding.radioButtonDeathRayHex, binding.radioButtonOmniBulletHex, binding.radioButtonRemoveHex)) {
            picker.selection.visibility = View.INVISIBLE

        }
        view.selection.visibility = View.VISIBLE
        highlightEditorTips(getHexTypeBasedOnHexButtonId(view.id))
    }

    private fun onHexTypeButtonLongClicked(view: HexPicker) {
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
        binding.editorCanvasView.selectedCellType = type
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

                binding.editorCanvasView.pointToHex(event.x, event.y, draggedHex)
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
        binding.editorCanvasView.tipHexes = null
        when (view.id) {
            R.id.buttonRotateCellLeft -> {
                presenter.rotateCellLeft()
                binding.editorCanvasView.tipHexes = presenter.getTipHexes(binding.editorCanvasView.selectedCellType)
                binding.editorCanvasView.invalidate()
            }
            R.id.buttonRotateCellRight -> {
                presenter.rotateCellRight()
                binding.editorCanvasView.tipHexes = presenter.getTipHexes(binding.editorCanvasView.selectedCellType)
                binding.editorCanvasView.invalidate()
            }
        }
    }

    private fun switchCellLogicEditorViews() {
        isCellLogicViewsShown = !isCellLogicViewsShown

        val cellLogicVisibility = isCellLogicViewsShown
        val editorVisibility = !isCellLogicViewsShown

        binding.buttonSwitchScreen.setImageDrawable(
                ContextCompat.getDrawable(requireContext(),
                                          if (!isCellLogicViewsShown) R.drawable.ic_button_brain
                                          else R.drawable.ic_button_editor)
        )

        /// TODO: energy hex is available not from the beginning
        for (view in arrayListOf<View>(binding.radioButtonLifeHex, binding.radioButtonAttackHex, binding.radioButtonEnergyHex,
                                       binding.radioButtonRemoveHex, binding.editorCanvasView,
                                       binding.buttonRotateCellLeft, binding.buttonRotateCellRight, binding.radioButtonDeathRayHex,
                                       binding.radioButtonOmniBulletHex,
                                       binding.buttonTransformHexes, binding.buttonTransformAttackToLifeHex, binding.buttonTransformDeathRayToLifeHex,
                                       binding.buttonTransformOmniBulletToLifeHex, binding.buttonTransformLifeToOmniBulletHex,
                                       binding.buttonTransformEnergyToLifeHex, binding.buttonTransformLifeToAttackHex,
                                       binding.buttonTransformLifeToDeathRayHex, binding.buttonTransformLifeToEnergyHex,
                                       binding.buttonClearCell)) {
            view.visible = editorVisibility
        }

        binding.textHeroHistory.visible = !cellLogicVisibility and binding.textHeroHistory.visible

        for (view in arrayListOf<View>(binding.recyclerViewRulesList, binding.recyclerViewConditionsList,
                                       binding.buttonAddNewRule, binding.buttonAddNewCondition))
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
        for (view in arrayListOf<View>(binding.editorCanvasView, binding.buttonRotateCellLeft, binding.buttonRotateCellRight))
            view.visible = !isHexesTransformShown

        for (view in arrayListOf<View>(binding.buttonTransformAttackToLifeHex, binding.buttonTransformDeathRayToLifeHex,
                                       binding.buttonTransformEnergyToLifeHex, binding.buttonTransformLifeToAttackHex,
                                       binding.buttonTransformLifeToDeathRayHex, binding.buttonTransformLifeToEnergyHex,
                                       binding.buttonTransformOmniBulletToLifeHex, binding.buttonTransformLifeToOmniBulletHex))
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
