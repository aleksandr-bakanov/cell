package bav.onecell.heroscreen

import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.model.GameRules
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Action
import bav.onecell.model.cell.logic.Condition
import bav.onecell.model.cell.logic.Rule
import bav.onecell.model.hexes.Hex
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class HeroScreenPresenter(
        private val view: HeroScreen.View,
        private val gameRules: GameRules,
        private val gameState: Common.GameState,
        private val cellRepository: RepositoryContract.CellRepo,
        private val resourceProvider: Common.ResourceProvider) : HeroScreen.Presenter {

    companion object {
        private const val TAG = "HeroScreenPresenter"
    }

    private var cell: Cell? = null
    private var cellDisposable: Disposable? = null
    private val cellProvider = BehaviorSubject.create<Cell>()
    private val backgroundFieldRadiusProvider = BehaviorSubject.create<Int>()
    private var rules: MutableList<Rule>? = null
    private val rulesNotifier = PublishSubject.create<Unit>()
    private var currentCellIndex = -1
    private var currentlyEditedRule: Rule? = null
    private var currentRuleIndex: Int? = null
    private var currentConditionIndex: Int? = null
    private var currentlyEditedCondition: Condition? = null
    private val conditionsNotifier = PublishSubject.create<Unit>()
    private val pickerOptionsNotifier = PublishSubject.create<Unit>()
    private var pickerOptionsSource: List<Pair<Int, () -> Unit? >>? = null

    override fun initialize(cellIndex: Int) {
        if (cellIndex != currentCellIndex) {
            cellDisposable?.let { if (!it.isDisposed) it.dispose() }
            cellDisposable = cellRepository.loadFromStore()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        cell?.let { currentCell -> cellRepository.storeCell(currentCell.data) }
                        cell = cellRepository.getCell(cellIndex)
                        cell?.let { c -> view.setCellName(resourceProvider.getString(c.data.name) ?: "") }
                        updateHexBucketCounts()
                        rules = cell?.data?.rules
                        rulesNotifier.onNext(Unit)
                        currentlyEditedRule = null
                        currentRuleIndex = null
                        currentConditionIndex = null
                        conditionsNotifier.onNext(Unit)
                        setPickerOptionsSource(null)
                        backgroundFieldRadiusProvider.onNext(4)
                        cellProvider.onNext(cell!!)
                        view.updateAvatars()
                        cellDisposable?.dispose()
                    }
        }
    }

    //region Picker.Presenter methods
    override fun pickerOptionOnClick(id: Int) {
        pickerOptionsSource?.let {
            it.find { item -> item.first == id }?.second?.let { func ->
                func()
                /// TODO: notify only necessary recycler views
                rulesNotifier.onNext(Unit)
                conditionsNotifier.onNext(Unit)
            }
        }
    }
    //endregion

    //region Conditions.Presenter methods
    override fun initializeConditionList(cellIndex: Int, ruleIndex: Int) {
        setCurrentRule(ruleIndex)
        conditionsNotifier.onNext(Unit)
        setPickerOptionsSource(null)
    }

    override fun conditionsUpdateNotifier(): Observable<Unit> = conditionsNotifier

    override fun conditionsCount(): Int = currentlyEditedRule?.size() ?: -1

    override fun createNewCondition(): Boolean {
        currentlyEditedRule?.let {
            it.addCondition(Condition())
            setCurrentCondition(it.size() - 1)
            conditionsNotifier.onNext(Unit)
            rulesNotifier.onNext(Unit)
        }
        return currentlyEditedRule != null
    }

    override fun removeCondition(index: Int) {
        currentlyEditedRule?.let {
            it.removeConditionAt(index)
            currentConditionIndex?.let { currentIndex ->
                if (currentIndex == index) {
                    clearCurrentCondition()
                }
                else if (currentIndex < index) {
                    // Do nothing
                }
                else {
                    currentConditionIndex = currentIndex - 1
                }
            }
            conditionsNotifier.onNext(Unit)
            rulesNotifier.onNext(Unit)
            // TODO: close currently opened popup menu
        }
    }

    override fun chooseFieldToCheck(conditionIndex: Int) {
        if (setCurrentCondition(conditionIndex))
            setPickerOptionsSource(cellRuleConditionFieldsToCheck)
    }

    override fun chooseOperation(conditionIndex: Int): Int {
        return if (setCurrentCondition(conditionIndex)) {
            setPickerOptionsSource(getCellRuleConditionOperations())
            getCellRuleConditionOperationsMenu()
        }
        else 0
    }

    override fun chooseExpectedValue(conditionIndex: Int): Int {
        return if (setCurrentCondition(conditionIndex)) {
            setPickerOptionsSource(getCellRuleConditionExpectedValue())
            getCellRuleConditionExpectedValueMenu()
        }
        else 0
    }

    override fun getCondition(index: Int): Condition? = currentlyEditedRule?.getCondition(index)

    override fun getCurrentConditionIndex(): Int? = currentConditionIndex

    override fun setFieldToCheckForCurrentCondition(fieldToCheckId: Int) {
        currentlyEditedCondition?.let {
            cellRuleConditionFieldsToCheck.find { item -> item.first == fieldToCheckId }?.second?.let { func ->
                func()
                /// TODO: notify only necessary recycler views
                rulesNotifier.onNext(Unit)
                conditionsNotifier.onNext(Unit)
            }
        }
    }

    override fun setOperationForCurrentCondition(operationId: Int) {
        currentlyEditedCondition?.let {
            for (operations in arrayListOf(cellRuleConditionOperationsDirectionToNearestEnemy,
                                           cellRuleConditionOperationsDistanceToNearestEnemy)) {
                operations.find { item -> item.first == operationId }?.second?.let { func ->
                    func()
                    /// TODO: notify only necessary recycler views
                    rulesNotifier.onNext(Unit)
                    conditionsNotifier.onNext(Unit)
                }
            }
        }
    }

    override fun setExpectedValueForCurrentCondition(expectedValueId: Int) {
        currentlyEditedCondition?.let {
            for (operations in arrayListOf(cellRuleConditionExpectedValuesDirectionToNearestEnemy,
                                           cellRuleConditionExpectedValuesDistanceToNearestEnemy)) {
                operations.find { item -> item.first == expectedValueId }?.second?.let { func ->
                    func()
                    /// TODO: notify only necessary recycler views
                    rulesNotifier.onNext(Unit)
                    conditionsNotifier.onNext(Unit)
                }
            }
        }
    }
    //endregion

    //region Rules.Presenter methods
    override fun rulesCount(): Int = rules?.size ?: 0

    override fun createNewRule() {
        rules?.let {
            it.add(Rule())
            rulesNotifier.onNext(Unit)
        }
    }

    override fun removeRule(index: Int) {
        rules?.let {
            if (index in 0 until it.size) {
                it.removeAt(index)
                currentRuleIndex?.let { currentIndex ->
                    if (currentIndex == index) {
                        clearCurrentRule()
                    }
                    else if (currentIndex < index) {
                        // Do nothing
                    }
                    else {
                        currentRuleIndex = currentIndex - 1
                    }
                }
            }
            rulesNotifier.onNext(Unit)
        }
    }

    override fun swapRules(first: Int, second: Int) {
        rules?.let {
            val tmp = it[first]
            it[first] = it[second]
            it[second] = tmp
        }
    }

    override fun rulesUpdateNotifier(): Observable<Unit> = rulesNotifier

    override fun openConditionsList(ruleIndex: Int) {
        rules?.let {
            if (ruleIndex >= 0 && ruleIndex < it.size) {
                initializeConditionList(currentCellIndex, ruleIndex)
            }
        }
    }

    override fun openActionEditor(ruleIndex: Int) {
        rules?.let {
            if (ruleIndex >= 0 && ruleIndex < it.size) {
                initializeRuleActionChoice(ruleIndex)
            }
        }
    }

    override fun getRule(index: Int): Rule? {
        rules?.let {
            if (index >= 0 && index < it.size) {
                return it[index]
            }
        }
        return null
    }
    //endregion

    //region Editor.Presenter methods
    override fun addHexToCell(hex: Hex) {
        cell?.let {
            if (gameRules.isAllowedToAddHexIntoCell(it, hex) and it.hasHexesInBucket(hex.type)) {
                decreaseHexesInBucket(hex.type)
                it.addHex(hex)
                it.evaluateCellHexesPower()
                view.updateCellRepresentation()
                view.highlightTips(hex.type)
                view.setNextSceneButtonVisibility(it.data.hexes.isNotEmpty())
            }
        }
    }

    override fun removeHexFromCell(hex: Hex) {
        cell?.let {
            if (gameRules.isAllowedToRemoveHexFromCell(it, hex)) {
                val hexType = it.data.hexes[Pair(hex.q, hex.r)]?.type ?: Hex.Type.REMOVE
                increaseHexesInBucket(hexType)
                it.removeHex(hex)
                it.evaluateCellHexesPower()
                view.updateCellRepresentation()
                view.highlightTips(Hex.Type.REMOVE)
                view.setNextSceneButtonVisibility(it.data.hexes.isNotEmpty())
            }
        }
    }

    override fun rotateCellLeft() {
        cell?.let {
            it.rotateLeft()
            view.updateCellRepresentation()
            it.evaluateCellHexesPower()
        }
    }

    override fun rotateCellRight() {
        cell?.let {
            it.rotateRight()
            view.updateCellRepresentation()
            it.evaluateCellHexesPower()
        }
    }

    override fun getCellProvider(): Observable<Cell> = cellProvider
    override fun getBackgroundCellRadiusProvider(): Observable<Int> = backgroundFieldRadiusProvider

    override fun getTipHexes(type: Hex.Type): Collection<Hex> {
        cell?.let { c ->
            c.updateOutlineHexes()
            return when (type) {
                Hex.Type.REMOVE -> mutableSetOf() /*c.data.hexes.values.filter { hex -> !gameRules.isAllowedToRemoveHexFromCell(c, hex) }*/
                else -> c.getOutlineHexes().filter { hex -> gameRules.isAllowedToAddHexIntoCell(c, hex.withType(type)) }
            }

        }
        return mutableSetOf()
    }

    override fun getHexInBucketCount(type: Hex.Type): Int = cell?.data?.hexBucket?.get(type.ordinal) ?: 0
    //endregion

    //region HeroScreen.Presenter methods
    override fun openMainMenu() {
        cell?.let { cellRepository.storeCell(it.data) }
        //router.goToMain()
    }

    override fun getCurrentlySelectedRuleIndex(): Int? = currentRuleIndex

    override fun getCellCount(): Int {
        return if (gameState.isDecisionPositive(Common.GameState.ALL_CHARACTERS_AVAILABLE)) cellRepository.cellsCount()
        else if (gameState.isDecisionPositive(Common.GameState.AIMA_AVAILABLE)) 3
        else if (gameState.isDecisionPositive(Common.GameState.ZOI_AVAILABLE)) 2
        else 1
    }

    override fun isThereAnyEmptyCell(): Boolean {
        for (i in 0 until cellRepository.cellsCount()) {
            cellRepository.getCell(i)?.let { if (it.data.hexes.isEmpty()) return true }
        }
        return false
    }

    override fun transformLifeHexToAttack() {
        transformHexes(Hex.Type.LIFE, Hex.Type.ATTACK, Hex.TransformPrice.LIFE_TO_ATTACK.value, 1)
    }

    override fun transformLifeHexToEnergy() {
        transformHexes(Hex.Type.LIFE, Hex.Type.ENERGY, Hex.TransformPrice.LIFE_TO_ENERGY.value, 1)
    }

    override fun transformLifeHexToDeathRay() {
        transformHexes(Hex.Type.LIFE, Hex.Type.DEATH_RAY, Hex.TransformPrice.LIFE_TO_DEATH_RAY.value, 1)
    }

    override fun transformLifeHexToOmniBullet() {
        transformHexes(Hex.Type.LIFE, Hex.Type.OMNI_BULLET, Hex.TransformPrice.LIFE_TO_OMNI_BULLET.value, 1)
    }

    override fun transformAttackHexToLife() {
        transformHexes(Hex.Type.ATTACK, Hex.Type.LIFE, 1, Hex.TransformPrice.LIFE_TO_ATTACK.value)
    }

    override fun transformEnergyHexToLife() {
        transformHexes(Hex.Type.ENERGY, Hex.Type.LIFE, 1, Hex.TransformPrice.LIFE_TO_ENERGY.value)
    }

    override fun transformDeathRayHexToLife() {
        transformHexes(Hex.Type.DEATH_RAY, Hex.Type.LIFE, 1, Hex.TransformPrice.LIFE_TO_DEATH_RAY.value)
    }

    override fun transformOmniBulletHexToLife() {
        transformHexes(Hex.Type.OMNI_BULLET, Hex.Type.LIFE, 1, Hex.TransformPrice.LIFE_TO_OMNI_BULLET.value)
    }
    //endregion

    //region Private methods
    private fun transformHexes(from: Hex.Type, to: Hex.Type, price: Int, income: Int) {
        cell?.let {
            val currentFromCount = it.data.hexBucket.getOrElse(from.ordinal, Consts.ZERO)
            if (currentFromCount >= price) {
                val newFromCount = currentFromCount - price
                val newToCount = it.data.hexBucket.getOrElse(to.ordinal, Consts.ZERO) + income
                it.data.hexBucket[from.ordinal] = newFromCount
                it.data.hexBucket[to.ordinal] = newToCount
                view.updateHexesInBucket(from, newFromCount)
                view.updateHexesInBucket(to, newToCount)
            }
        }
    }

    private fun notifyHexesInBucket(type: Hex.Type, count: Int) {
        cell?.let {
            val hexCount = (it.data.hexBucket[type.ordinal] ?: 0) + count
            it.data.hexBucket[type.ordinal] = hexCount
            view.updateHexesInBucket(type, hexCount)
        }
    }

    private fun increaseHexesInBucket(type: Hex.Type) {
        notifyHexesInBucket(type, 1)
    }

    private fun decreaseHexesInBucket(type: Hex.Type) {
        notifyHexesInBucket(type, -1)
    }

    private fun notifyAllHexesInBucket() {
        cell?.let {
            for (type in Hex.Type.values()) {
                view.updateHexesInBucket(type, it.data.hexBucket[type.ordinal] ?: 0)
            }
        }
    }

    private fun initializeRuleActionChoice(ruleIndex: Int) {
        setCurrentRule(ruleIndex)
        setPickerOptionsSource(cellRuleActions)
    }

    private fun setPickerOptionsSource(source: List<Pair<Int, () -> Unit? >>?) {
        pickerOptionsSource = source
        pickerOptionsNotifier.onNext(Unit)
    }

    private fun setCurrentRule(ruleIndex: Int) {
        currentlyEditedRule = rules?.get(ruleIndex)
        currentlyEditedRule?.let {
            currentRuleIndex = ruleIndex
            rulesNotifier.onNext(Unit)
            clearCurrentCondition()
        }
    }

    private fun setCurrentCondition(conditionIndex: Int): Boolean {
        currentlyEditedCondition = currentlyEditedRule?.getCondition(conditionIndex)
        currentlyEditedCondition?.let { currentConditionIndex = conditionIndex; conditionsNotifier.onNext(Unit) }
        return currentlyEditedCondition != null
    }

    private fun clearCurrentCondition() {
        currentlyEditedCondition = null
        currentConditionIndex = null
        setPickerOptionsSource(null)
        conditionsNotifier.onNext(Unit)
    }

    private fun clearCurrentRule() {
        currentlyEditedRule = null
        currentRuleIndex = null
        rulesNotifier.onNext(Unit)
        clearCurrentCondition()
        setPickerOptionsSource(null)
    }

    private fun updateHexBucketCounts() {
        cell?.let {
            for (type in Hex.Type.values()) {
                view.updateHexesInBucket(type, it.data.hexBucket[type.ordinal] ?: 0)
            }
        }
    }
    //endregion

    //region Picker options
    private val cellRuleActions = arrayListOf(
            Pair(R.id.action_rotate_north, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.N.ordinal }}),
            Pair(R.id.action_rotate_north_east, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.NE.ordinal }}),
            Pair(R.id.action_rotate_south_east, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.SE.ordinal }}),
            Pair(R.id.action_rotate_south, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.S.ordinal }}),
            Pair(R.id.action_rotate_south_west, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.SW.ordinal }}),
            Pair(R.id.action_rotate_north_west, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.NW.ordinal }})
    )

    private val cellRuleConditionFieldsToCheck = arrayListOf(
            Pair(R.id.field_to_check_direction_to_nearest_enemy, { currentlyEditedCondition?.let { it.setToDefault(); it.fieldToCheck = Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY }}),
            Pair(R.id.field_to_check_distance_to_nearest_enemy, { currentlyEditedCondition?.let { it.setToDefault(); it.fieldToCheck = Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY }})
    )

    private val cellRuleConditionOperationsDirectionToNearestEnemy = arrayListOf(
            Pair(R.id.operation_direction_to_nearest_enemy_equals, {
                currentlyEditedCondition?.let { it.operation = Condition.Operation.EQUALS }
            })
    )

    private val cellRuleConditionOperationsDistanceToNearestEnemy = arrayListOf(
            Pair(R.id.operation_distance_to_nearest_enemy_equals, { currentlyEditedCondition?.let { it.operation = Condition.Operation.EQUALS }}),
            Pair(R.id.operation_distance_to_nearest_enemy_less_than, { currentlyEditedCondition?.let { it.operation = Condition.Operation.LESS_THAN }}),
            Pair(R.id.operation_distance_to_nearest_enemy_greater_than, { currentlyEditedCondition?.let { it.operation = Condition.Operation.GREATER_THAN }})
    )

    private val cellRuleConditionExpectedValuesDirectionToNearestEnemy = arrayListOf(
            Pair(R.id.expected_value_direction_to_nearest_enemy_north, { currentlyEditedCondition?.let { it.expected = Cell.Direction.N.ordinal }}),
            Pair(R.id.expected_value_direction_to_nearest_enemy_north_east, { currentlyEditedCondition?.let { it.expected = Cell.Direction.NE.ordinal }}),
            Pair(R.id.expected_value_direction_to_nearest_enemy_south_east, { currentlyEditedCondition?.let { it.expected = Cell.Direction.SE.ordinal }}),
            Pair(R.id.expected_value_direction_to_nearest_enemy_south, { currentlyEditedCondition?.let { it.expected = Cell.Direction.S.ordinal }}),
            Pair(R.id.expected_value_direction_to_nearest_enemy_south_west, { currentlyEditedCondition?.let { it.expected = Cell.Direction.SW.ordinal }}),
            Pair(R.id.expected_value_direction_to_nearest_enemy_north_west, { currentlyEditedCondition?.let { it.expected = Cell.Direction.NW.ordinal }})
    )

    private val cellRuleConditionExpectedValuesDistanceToNearestEnemy = arrayListOf(
            Pair(R.id.expected_value_distance_to_nearest_enemy_zero, { currentlyEditedCondition?.let { it.expected = 0 }}),
            Pair(R.id.expected_value_distance_to_nearest_enemy_one, { currentlyEditedCondition?.let { it.expected = 1 }}),
            Pair(R.id.expected_value_distance_to_nearest_enemy_two, { currentlyEditedCondition?.let { it.expected = 2 }}),
            Pair(R.id.expected_value_distance_to_nearest_enemy_three, { currentlyEditedCondition?.let { it.expected = 3 }}),
            Pair(R.id.expected_value_distance_to_nearest_enemy_four, { currentlyEditedCondition?.let { it.expected = 4 }}),
            Pair(R.id.expected_value_distance_to_nearest_enemy_five, { currentlyEditedCondition?.let { it.expected = 5 }}),
            Pair(R.id.expected_value_distance_to_nearest_enemy_six, { currentlyEditedCondition?.let { it.expected = 6 }}),
            Pair(R.id.expected_value_distance_to_nearest_enemy_seven, { currentlyEditedCondition?.let { it.expected = 7 }}),
            Pair(R.id.expected_value_distance_to_nearest_enemy_eight, { currentlyEditedCondition?.let { it.expected = 8 }}),
            Pair(R.id.expected_value_distance_to_nearest_enemy_nine, { currentlyEditedCondition?.let { it.expected = 9 }})
    )

    private fun getCellRuleConditionOperations(): List<Pair<Int, () -> Unit? >>? {
        return currentlyEditedCondition?.let { condition ->
            when (condition.fieldToCheck) {
                Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> cellRuleConditionOperationsDirectionToNearestEnemy
                Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> cellRuleConditionOperationsDistanceToNearestEnemy
                else -> null
            }
        }
    }

    private fun getCellRuleConditionOperationsMenu(): Int {
        return currentlyEditedCondition?.let { condition ->
            when (condition.fieldToCheck) {
                Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> R.menu.condition_operations_direction_to_nearest_enemy
                Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> R.menu.condition_operations_distance_to_nearest_enemy
                else -> 0
            }
        } ?: 0
    }

    private fun getCellRuleConditionExpectedValue(): List<Pair<Int, () -> Unit? >>? {
        return currentlyEditedCondition?.let { condition ->
            when (condition.fieldToCheck) {
                Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> cellRuleConditionExpectedValuesDirectionToNearestEnemy
                Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> cellRuleConditionExpectedValuesDistanceToNearestEnemy
                else -> null
            }
        }
    }

    private fun getCellRuleConditionExpectedValueMenu(): Int {
        return currentlyEditedCondition?.let { condition ->
            when (condition.fieldToCheck) {
                Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> R.menu.condition_expected_values_direction_to_nearest_enemy
                Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> R.menu.condition_expected_values_distance_to_nearest_enemy
                else -> 0
            }
        } ?: 0
    }
    //endregion
}
