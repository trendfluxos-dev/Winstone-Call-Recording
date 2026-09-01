package com.example

import com.example.telephony.PhoneNumberUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberUtilsTest {

    @Test
    fun testBangladeshNumberNormalization() {
        // Local 11-digit starting with 01
        assertEquals("+8801711223344", PhoneNumberUtils.normalize("01711223344"))

        // Already with +880
        assertEquals("+8801711223344", PhoneNumberUtils.normalize("+8801711223344"))

        // With 880 prefix without plus
        assertEquals("+8801711223344", PhoneNumberUtils.normalize("8801711223344"))

        // Formatted with hyphens and spaces
        assertEquals("+8801711223344", PhoneNumberUtils.normalize("01711-223344"))
        assertEquals("+8801711223344", PhoneNumberUtils.normalize("+880 1711 223344"))
    }

    @Test
    fun testPhoneNumberMatching() {
        assertTrue(PhoneNumberUtils.isMatch("01711223344", "+8801711223344"))
        assertTrue(PhoneNumberUtils.isMatch("8801711223344", "01711223344"))
        assertTrue(PhoneNumberUtils.isMatch("+8801711223344", "+8801711223344"))
    }

    @Test
    fun testDisplayFormatting() {
        val formatted = PhoneNumberUtils.formatDisplay("+8801711223344")
        assertEquals("+880 1711-223344", formatted)
    }
}
