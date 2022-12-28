package com.joyy.android_project

import org.junit.Test

import org.junit.Assert.*
import java.nio.charset.Charset
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class StringUnitTest {
    val EXPORT_LIZARD_DB_ACTION = byteArrayOf(
        111,
        114,
        103,
        46,
        111,
        100,
        105,
        110,
        46,
        101,
        120,
        112,
        111,
        114,
        116,
        95,
        100,
        98
    )

    @Test
    fun addition_isCorrect() {
        var string = String(EXPORT_LIZARD_DB_ACTION)
        println(string)
        var s = "org.lizard.export_db"
        println(s.toByteArray().contentToString())
        println(EXPORT_LIZARD_DB_ACTION.contentToString())
    }
}