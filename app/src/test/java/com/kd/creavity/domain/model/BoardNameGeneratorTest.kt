package com.kd.creavity.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BoardNameGeneratorTest {

    @Test
    fun testGenerateProducesTwoWords() {
        for (i in 0 until 100) {
            val title = BoardNameGenerator.generate()
            val words = title.split(" ")
            assertEquals("Title '$title' should consist of exactly two words", 2, words.size)
            assertTrue("First word should be capitalized", words[0][0].isUpperCase())
            assertTrue("Second word should be capitalized", words[1][0].isUpperCase())
        }
    }

    @Test
    fun testAvoidsExistingCollisions() {
        val existing = mutableSetOf<String>()
        val random = Random(42)

        for (i in 0 until 20) {
            val title = BoardNameGenerator.generate(existingTitles = existing, random = random)
            assertTrue("Generated title '$title' should not collide with existing set", !existing.contains(title))
            existing.add(title)
        }
    }

    @Test
    fun testBoardDefaultTitleIsTwoWords() {
        val board = Board()
        val words = board.title.split(" ")
        assertEquals("Board default title should have 2 words", 2, words.size)
        assertNotEquals("Untitled Board", board.title)
    }
}
