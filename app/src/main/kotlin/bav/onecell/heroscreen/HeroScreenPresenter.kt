package bav.onecell.heroscreen

import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.router.Router
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
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router,
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
            cellDisposable?.dispose()
            cellDisposable = cellRepository.loadFromStore()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        cell?.let { currentCell -> cellRepository.storeCell(currentCell) }
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
                    }
        }
    }

    //region Picker.Presenter methods
    override fun pickerOptionsCount(): Int = pickerOptionsSource?.size ?: 0

    override fun pickerOptionOnClick(position: Int) {
        pickerOptionsSource?.let {
            if (position >= 0 && position < it.size) {
                it[position].second()
                /// TODO: notify only necessary recycler views
                rulesNotifier.onNext(Unit)
                conditionsNotifier.onNext(Unit)
            }
        }
    }

    override fun getPickerOptionTitle(position: Int): Int {
        pickerOptionsSource?.let {
            if (position >= 0 && position < it.size) return it[position].first
        }
        return 0
    }

    override fun optionsUpdateNotifier(): Observable<Unit> = pickerOptionsNotifier
    //endregion

    //region Conditions.Presenter methods
    override fun initializeConditionList(cellIndex: Int, ruleIndex: Int) {
        setCurrentRule(ruleIndex)
        conditionsNotifier.onNext(Unit)
        setPickerOptionsSource(null)
    }

    override fun conditionsUpdateNotifier(): Observable<Unit> = conditionsNotifier

    override fun conditionsCount(): Int = currentlyEditedRule?.size() ?: 0

    override fun createNewCondition() {
        currentlyEditedRule?.let {
            it.addCondition(Condition())
            conditionsNotifier.onNext(Unit)
            rulesNotifier.onNext(Unit)
        }
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
        }
    }

    override fun openConditionEditor(conditionIndex: Int, whatToEdit: Int) {
        // Do nothing, planned to remove
    }

    private val directionValues: Array<String> = arrayOf(
            Cell.Direction.N.toString(),
            Cell.Direction.NE.toString(),
            Cell.Direction.SE.toString(),
            Cell.Direction.S.toString(),
            Cell.Direction.SW.toString(),
            Cell.Direction.NW.toString()
    )

    override fun provideActionDialogValues(): Array<String> = directionValues

    override fun chooseFieldToCheck(conditionIndex: Int) {
        if (setCurrentCondition(conditionIndex))
            setPickerOptionsSource(cellRuleConditionFieldsToCheck)
    }

    override fun chooseOperation(conditionIndex: Int) {
        if (setCurrentCondition(conditionIndex))
            setPickerOptionsSource(getCellRuleConditionOperations())
    }

    override fun chooseExpectedValue(conditionIndex: Int) {
        if (setCurrentCondition(conditionIndex))
            setPickerOptionsSource(getCellRuleConditionExpectedValue())
    }

    override fun getCondition(index: Int): Condition? = currentlyEditedRule?.getCondition(index)

    override fun getCurrentConditionIndex(): Int? = currentConditionIndex
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

    override fun increaseSelectedRulePriority() {
        rules?.let { rules ->
            currentRuleIndex?.let { index ->
                if (index > 0) {
                    val tmp = rules[index]
                    rules[index] = rules[index - 1]
                    rules[index - 1] = tmp
                    currentRuleIndex = index - 1
                    rulesNotifier.onNext(Unit)
                }
            }
        }
    }

    override fun decreaseSelectedRulePriority() {
        rules?.let { rules ->
            currentRuleIndex?.let { index ->
                if (index < rules.size - 1) {
                    val tmp = rules[index]
                    rules[index] = rules[index + 1]
                    rules[index + 1] = tmp
                    currentRuleIndex = index + 1
                    rulesNotifier.onNext(Unit)
                }
            }
        }
    }
    //endregion

    //region Editor.Presenter methods
    override fun addHexToCell(hex: Hex) {
        cell?.let {
            if (gameRules.isAllowedToAddHexIntoCell(it, hex) and it.hasHexesInBucket(hex.type)) {
                decreaseHexesInBucket(hex.type)
                it.addHex(hex)
                it.evaluateCellHexesPower()
                view.highlightTips(hex.type)
            }
        }
    }

    override fun removeHexFromCell(hex: Hex) {
        cell?.let {
            if (gameRules.isAllowedToRemoveHexFromCell(it, hex)) {
                val hexType = it.data.hexes[hex.hashCode()]?.type ?: Hex.Type.REMOVE
                increaseHexesInBucket(hexType)
                it.removeHex(hex)
                it.evaluateCellHexesPower()
            }
        }
    }

    override fun rotateCellLeft() {
        cell?.let {
            it.rotateLeft()
            it.evaluateCellHexesPower()
        }
    }

    override fun rotateCellRight() {
        cell?.let {
            it.rotateRight()
            it.evaluateCellHexesPower()
        }
    }

    override fun getCellProvider(): Observable<Cell> = cellProvider
    override fun getBackgroundCellRadiusProvider(): Observable<Int> = backgroundFieldRadiusProvider

    override fun getTipHexes(type: Hex.Type): Collection<Hex> {
        cell?.let { c ->
            c.updateOutlineHexes()
            return c.getOutlineHexes().filter { hex -> gameRules.isAllowedToAddHexIntoCell(c, hex.withType(type)) }
        }
        return mutableSetOf()
    }

    override fun getHexInBucketCount(type: Hex.Type): Int = cell?.data?.hexBucket?.get(type.ordinal) ?: 0
    //endregion

    //region HeroScreen.Presenter methods
    override fun openMainMenu() {
        cell?.let { cellRepository.storeCell(it) }
        //router.goToMain()
    }

    override fun getCurrentlySelectedRuleIndex(): Int? = currentRuleIndex

    override fun getCellCount(): Int = cellRepository.cellsCount()

    override fun transformLifeHexToAttack() {
        transformHexes(Hex.Type.LIFE, Hex.Type.ATTACK, Hex.TransformPrice.LIFE_TO_ATTACK.value, 1)
    }

    override fun transformLifeHexToEnergy() {
        transformHexes(Hex.Type.LIFE, Hex.Type.ENERGY, Hex.TransformPrice.LIFE_TO_ENERGY.value, 1)
    }

    override fun transformLifeHexToDeathRay() {
        transformHexes(Hex.Type.LIFE, Hex.Type.DEATH_RAY, Hex.TransformPrice.LIFE_TO_DEATH_RAY.value, 1)
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
        updatePickerBackground(source)
        pickerOptionsNotifier.onNext(Unit)
    }

    private fun updatePickerBackground(source: List<Pair<Int, () -> Unit? >>?) {
        view.setPickerBackground(
                when (source) {
                    cellRuleActions -> R.color.heroScreenSelectedRuleBackgroundColor
                    cellRuleConditionFieldsToCheck,
                    cellRuleConditionOperationsDirectionToNearestEnemy,
                    cellRuleConditionExpectedValuesDirectionToNearestEnemy -> R.color.heroScreenSelectedConditionBackgroundColor
                    else -> R.color.heroScreenUnselectedRuleBackgroundColor
                }
        )
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
            Pair(R.string.utf_icon_north_direction, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.N.ordinal }}),
            Pair(R.string.utf_icon_north_east_direction, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.NE.ordinal }}),
            Pair(R.string.utf_icon_south_east_direction, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.SE.ordinal }}),
            Pair(R.string.utf_icon_south_direction, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.S.ordinal }}),
            Pair(R.string.utf_icon_south_west_direction, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.SW.ordinal }}),
            Pair(R.string.utf_icon_north_west_direction, { currentlyEditedRule?.action?.let { it.act = Action.Act.CHANGE_DIRECTION; it.value = Cell.Direction.NW.ordinal }})
    )

    private val cellRuleConditionFieldsToCheck = arrayListOf(
            Pair(R.string.utf_icon_direction_to_nearest_enemy, { currentlyEditedCondition?.let { it.setToDefault(); it.fieldToCheck = Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY }}),
            Pair(R.string.utf_icon_distance_to_nearest_enemy, { currentlyEditedCondition?.let { it.setToDefault(); it.fieldToCheck = Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY }})
    )

    private val cellRuleConditionOperationsDirectionToNearestEnemy = arrayListOf(
            Pair(R.string.utf_icon_equality, {
                currentlyEditedCondition?.let { it.operation = Condition.Operation.EQUALS }
            })
    )

    private val cellRuleConditionOperationsDistanceToNearestEnemy = arrayListOf(
            Pair(R.string.utf_icon_equality, { currentlyEditedCondition?.let { it.operation = Condition.Operation.EQUALS }}),
            Pair(R.string.utf_icon_less_than, { currentlyEditedCondition?.let { it.operation = Condition.Operation.LESS_THAN }}),
            Pair(R.string.utf_icon_greater_than, { currentlyEditedCondition?.let { it.operation = Condition.Operation.GREATER_THAN }})
    )

    private val cellRuleConditionExpectedValuesDirectionToNearestEnemy = arrayListOf(
            Pair(R.string.utf_icon_north_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.N.ordinal }}),
            Pair(R.string.utf_icon_north_east_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.NE.ordinal }}),
            Pair(R.string.utf_icon_south_east_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.SE.ordinal }}),
            Pair(R.string.utf_icon_south_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.S.ordinal }}),
            Pair(R.string.utf_icon_south_west_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.SW.ordinal }}),
            Pair(R.string.utf_icon_north_west_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.NW.ordinal }})
    )

    private val cellRuleConditionExpectedValuesDistanceToNearestEnemy = arrayListOf(
            Pair(R.string.utf_icon_digit_zero, { currentlyEditedCondition?.let { it.expected = 0 }}),
            Pair(R.string.utf_icon_digit_one, { currentlyEditedCondition?.let { it.expected = 1 }}),
            Pair(R.string.utf_icon_digit_two, { currentlyEditedCondition?.let { it.expected = 2 }}),
            Pair(R.string.utf_icon_digit_three, { currentlyEditedCondition?.let { it.expected = 3 }}),
            Pair(R.string.utf_icon_digit_four, { currentlyEditedCondition?.let { it.expected = 4 }}),
            Pair(R.string.utf_icon_digit_five, { currentlyEditedCondition?.let { it.expected = 5 }}),
            Pair(R.string.utf_icon_digit_six, { currentlyEditedCondition?.let { it.expected = 6 }}),
            Pair(R.string.utf_icon_digit_seven, { currentlyEditedCondition?.let { it.expected = 7 }}),
            Pair(R.string.utf_icon_digit_eight, { currentlyEditedCondition?.let { it.expected = 8 }}),
            Pair(R.string.utf_icon_digit_nine, { currentlyEditedCondition?.let { it.expected = 9 }})
    )

    private fun getCellRuleConditionOperations(): List<Pair<Int, () -> Unit? >>? {
        return currentlyEditedCondition?.let { condition ->
            when (condition.fieldToCheck) {
                Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> cellRuleConditionOperationsDirectionToNearestEnemy
                Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> cellRuleConditionOperationsDistanceToNearestEnemy
            }
        }
    }

    private fun getCellRuleConditionExpectedValue(): List<Pair<Int, () -> Unit? >>? {
        return currentlyEditedCondition?.let { condition ->
            when (condition.fieldToCheck) {
                Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> cellRuleConditionExpectedValuesDirectionToNearestEnemy
                Condition.FieldToCheck.DISTANCE_TO_NEAREST_ENEMY -> cellRuleConditionExpectedValuesDistanceToNearestEnemy
            }
        }
    }
    //endregion
}
