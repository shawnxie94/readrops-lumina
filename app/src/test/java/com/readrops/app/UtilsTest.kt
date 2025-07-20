package com.readrops.app

import com.readrops.app.util.Utils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilsTest {

    @Test
    fun truncateStringTest() {
        val result =
            Utils.truncateString("This is a very very long string with more than 30 characters", 30)

        assertEquals(33, result.length)
        assertTrue(result.contains("This is a very very long strin"))
        assertTrue(result.endsWith("..."))
    }
}