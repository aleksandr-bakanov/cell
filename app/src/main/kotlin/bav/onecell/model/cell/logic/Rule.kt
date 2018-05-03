package bav.onecell.model.cell.logic

import com.google.gson.Gson

/**
 * This class represents a set of conditions and appropriate action which should be performed
 * if all conditions are true.
 */
class Rule(private val conditions: MutableList<Condition> = mutableListOf(),
           var action: Action = Action()) {

    fun check(state: BattleState): Boolean {
        return conditions.map { it.check(state) }.all { it }
    }

    fun addCondition(condition: Condition) {
        conditions.add(condition)
    }

    fun removeConditionAt(index: Int) {
        if (index >= 0 && index < conditions.size) conditions.removeAt(index)
    }

    fun size(): Int = conditions.size

    fun toJson(): String {
        return Gson().toJson(this)
    }
}
