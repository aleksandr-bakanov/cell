package bav.onecell.heroscreen

import bav.onecell.R
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
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class HeroScreenPresenter(
        private val view: HeroScreen.View,
        private val gameRules: GameRules,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : HeroScreen.Presenter {

    companion object {
        private const val TAG = "HeroScreenPresenter"
    }

    private var cell: Cell? = null
    private val cellProvider = BehaviorSubject.create<Cell>()
    private val backgroundFieldRadiusProvider = BehaviorSubject.create<Int>()
    private var rules: MutableList<Rule>? = null
    private val rulesNotifier = PublishSubject.create<Unit>()
    private var currentCellIndex = -1
    private var currentlyEditedRule: Rule? = null
    private var currentlyEditedCondition: Condition? = null
    private val conditionsNotifier = PublishSubject.create<Unit>()
    private val pickerOptionsNotifier = PublishSubject.create<Unit>()
    private var pickerOptionsSource: List<Pair<Int, () -> Unit? >>? = null

    override fun initialize(cellIndex: Int) {
        if (cellIndex != currentCellIndex) {
            cellRepository.loadFromStore()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        cell?.let { currentCell -> cellRepository.storeCell(currentCell) }
                        cell = cellRepository.getCell(cellIndex)
                        rules = cell?.data?.rules
                        rulesNotifier.onNext(Unit)
                        currentlyEditedRule = null
                        conditionsNotifier.onNext(Unit)
                        backgroundFieldRadiusProvider.onNext(4)
                        cellProvider.onNext(cell!!)
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
        currentlyEditedRule = rules?.get(ruleIndex)
        conditionsNotifier.onNext(Unit)
    }

    override fun conditionsUpdateNotifier(): Observable<Unit> = conditionsNotifier

    override fun conditionsCount(): Int = currentlyEditedRule?.size() ?: 0

    override fun createNewCondition() {
        currentlyEditedRule?.let {
            it.addCondition(Condition())
            conditionsNotifier.onNext(Unit)
        }
    }

    override fun removeCondition(index: Int) {
        currentlyEditedRule?.let {
            it.removeConditionAt(index)
            setPickerOptionsSource(null)
            conditionsNotifier.onNext(Unit)
        }
    }

    override fun openConditionEditor(conditionIndex: Int, whatToEdit: Int) {
        currentlyEditedRule?.getCondition(conditionIndex)?.let {
            router.goToConditionEditor(it, whatToEdit)
        }
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
        currentlyEditedCondition = currentlyEditedRule?.getCondition(conditionIndex)
        currentlyEditedCondition?.let {
            setPickerOptionsSource(cellRuleConditionFieldsToCheck)
        }
    }

    override fun chooseOperation(conditionIndex: Int) {
        currentlyEditedCondition = currentlyEditedRule?.getCondition(conditionIndex)
        currentlyEditedCondition?.let {
            setPickerOptionsSource(getCellRuleConditionOperations())
        }
    }

    override fun chooseExpectedValue(conditionIndex: Int) {
        currentlyEditedCondition = currentlyEditedRule?.getCondition(conditionIndex)
        currentlyEditedCondition?.let {
            setPickerOptionsSource(getCellRuleConditionExpectedValue())
        }
    }

    override fun getCondition(index: Int): Condition? = currentlyEditedRule?.getCondition(index)
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
            if (index in 0 until it.size) it.removeAt(index)
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
    //endregion

    //region Editor.Presenter methods
    override fun addHexToCell(hex: Hex) {
        cell?.let {
            if (gameRules.isAllowedToAddHexIntoCell(it, hex) and gameRules.userHasEnoughMoney(it, hex)) {
                it.removeMoney(it.hexTypeToPrice(hex.type))
                it.addHex(hex)
                it.evaluateCellHexesPower()
                view.highlightTips(hex.type)
            }
        }
    }

    override fun removeHexFromCell(hex: Hex) {
        cell?.let {
            if (gameRules.isAllowedToRemoveHexFromCell(it, hex)) {
                it.addMoney(it.hexTypeToPrice(it.data.hexes[hex.hashCode()]?.type ?: Hex.Type.REMOVE))
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
    //endregion

    //region HeroScreen.Presenter methods
    override fun openMainMenu() {
        cell?.let { cellRepository.storeCell(it) }
        router.goToMain()
    }
    //endregion

    //region Private methods
    private fun initializeRuleActionChoice(ruleIndex: Int) {
        currentlyEditedRule = rules?.get(ruleIndex)
        setPickerOptionsSource(cellRuleActions)
    }

    private fun setPickerOptionsSource(source: List<Pair<Int, () -> Unit? >>?) {
        pickerOptionsSource = source
        pickerOptionsNotifier.onNext(Unit)
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
            Pair(R.string.utf_icon_direction_to_nearest_enemy, {
                currentlyEditedCondition?.let { it.setToDefault(); it.fieldToCheck = Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY }
            })
    )

    private fun getCellRuleConditionOperations(): List<Pair<Int, () -> Unit? >>? {
        return currentlyEditedCondition?.let { condition ->
            when (condition.fieldToCheck) {
                Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> arrayListOf(
                        /// TODO: move these array-lists to consts
                        Pair(R.string.utf_icon_equality, {
                            currentlyEditedCondition?.let { it.operation = Condition.Operation.EQUALS }
                        }))
            }
        }
    }

    private fun getCellRuleConditionExpectedValue(): List<Pair<Int, () -> Unit? >>? {
        return currentlyEditedCondition?.let { condition ->
            when (condition.fieldToCheck) {
                Condition.FieldToCheck.DIRECTION_TO_NEAREST_ENEMY -> arrayListOf(
                        /// TODO: move these array-lists to consts
                        Pair(R.string.utf_icon_north_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.N.ordinal }}),
                        Pair(R.string.utf_icon_north_east_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.NE.ordinal }}),
                        Pair(R.string.utf_icon_south_east_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.SE.ordinal }}),
                        Pair(R.string.utf_icon_south_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.S.ordinal }}),
                        Pair(R.string.utf_icon_south_west_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.SW.ordinal }}),
                        Pair(R.string.utf_icon_north_west_direction, { currentlyEditedCondition?.let { it.expected = Cell.Direction.NW.ordinal }})
                )
            }
        }
    }
    //endregion
}
