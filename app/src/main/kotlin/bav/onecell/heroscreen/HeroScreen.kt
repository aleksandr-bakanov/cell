package bav.onecell.heroscreen

import bav.onecell.editor.Editor
import bav.onecell.model.cell.logic.Condition
import bav.onecell.model.cell.logic.Rule
import bav.onecell.model.hexes.Hex
import io.reactivex.Observable

interface HeroScreen {
    interface View: Editor.View {
        fun setCellName(name: String)
        fun updateAvatars()
        fun updateHexesInBucket(type: Hex.Type, count: Int)
    }
    interface Presenter: Editor.Presenter {
        // Hero screen
        override fun initialize(cellIndex: Int)
        fun openMainMenu()
        fun increaseSelectedRulePriority()
        fun decreaseSelectedRulePriority()
        fun getCellCount(): Int

        fun transformLifeHexToAttack()
        fun transformLifeHexToEnergy()
        fun transformLifeHexToDeathRay()
        fun transformLifeHexToOmniBullet()
        fun transformAttackHexToLife()
        fun transformEnergyHexToLife()
        fun transformDeathRayHexToLife()
        fun transformOmniBulletHexToLife()

        // Conditions
        fun initializeConditionList(cellIndex: Int, ruleIndex: Int)
        fun conditionsUpdateNotifier(): Observable<Unit>
        fun conditionsCount(): Int
        fun createNewCondition()
        fun removeCondition(index: Int)
        fun chooseFieldToCheck(conditionIndex: Int)
        fun chooseOperation(conditionIndex: Int): Int
        fun chooseExpectedValue(conditionIndex: Int): Int
        fun getCondition(index: Int): Condition?
        fun getCurrentConditionIndex(): Int?

        // Picker
        fun pickerOptionOnClick(id: Int)

        // Rules
        fun rulesCount(): Int
        fun createNewRule()
        fun removeRule(index: Int)
        fun rulesUpdateNotifier(): Observable<Unit>
        fun openConditionsList(ruleIndex: Int)
        fun openActionEditor(ruleIndex: Int)
        fun getRule(index: Int): Rule?
        fun getCurrentlySelectedRuleIndex(): Int?
    }
}
