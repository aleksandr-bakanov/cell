package bav.onecell.model.hexes

import org.junit.Assert.assertEquals
import org.junit.Test

class HexTest {

    @Test(expected = IllegalArgumentException::class)
    fun constructorWithBrokenInvariantShouldThrow() {
        val hex = Hex(1, 2, 3)
    }

    @Test
    fun constructorWithValidInvariantShouldNotThrow() {
        val hex = Hex(-1, -2, 3)
    }

    @Test
    fun withTypeShouldSavePassedType() {
        assertEquals(Hex.Type.LIFE, Hex(0, 0, 0).withType(Hex.Type.LIFE).type)
    }

    @Test
    fun withPowerShouldSavePassedPower() {
        val power = 123
        assertEquals(power, Hex(0, 0, 0).withPower(power).power)
    }

    @Test
    fun cloneShouldCreateDeepCopyOfHex() {
        val type = Hex.Type.ENERGY
        val power = 123
        val q = 1
        val r = 2
        val s = -3
        val original = Hex(q, r, s).withType(type).withPower(power)
        val copy = original.clone()
        assertEquals(type, copy.type)
        assertEquals(power, copy.power)
        assertEquals(q, copy.q)
        assertEquals(r, copy.r)
        assertEquals(s, copy.s)
    }
}