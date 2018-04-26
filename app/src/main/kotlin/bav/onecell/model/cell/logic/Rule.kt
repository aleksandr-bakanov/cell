package bav.onecell.model.cell.logic

/**
 * This class represents a set of conditions and appropriate action which should be performed
 * if all conditions are true.
 */
class Rule(private val conditions: MutableList<Condition> = mutableListOf(),
           private val action: Action) {

    fun check(state: BattleState): Boolean {
        return conditions.map { it.check(state) }.all { it }
    }
}
