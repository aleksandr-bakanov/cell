package bav.onecell.model.cell.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RuleTest {

    @Test
    fun testRule() {
        val rule = Rule()
        rule.addCondition(Condition())
        rule.addCondition(Condition())
    }
}