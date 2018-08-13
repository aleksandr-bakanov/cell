package bav.onecell.model.cell.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import bav.onecell.common.router.Router

class RuleTest {

    @Test
    fun testRule() {
        val rule = Rule()
        rule.addCondition(Condition())
        rule.addCondition(Condition())
//        System.out.println(rule.toJson())
        System.out.println(Router.WindowType.CUT_SCENE.toString())
    }
}