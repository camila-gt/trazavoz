package com.trazavoz.domain.usecase

import com.trazavoz.domain.model.Word
import org.junit.Assert.*
import org.junit.Test

class PrepareReadingWordUseCaseTest {
    private val prepare = PrepareReadingWordUseCase(SplitSyllablesUseCase())
    private fun word(text: String, syllables: List<String>) = Word(1, text, "", "", syllables, "", null)

    @Test fun `keeps matching stored syllables`() {
        val result = prepare(word("mesa", listOf("me", "sa")))!!
        assertEquals("MESA", result.displayText)
        assertEquals(listOf("ME", "SA"), result.syllables.map { it.text })
        assertEquals(listOf("M", "E", "S", "A"), result.letters.map { it.text })
    }

    @Test fun `uses existing syllabifier for empty blank or mismatching syllables`() {
        listOf(emptyList(), listOf(""), listOf("CA", "SA")).forEach { stored ->
            val result = prepare(word("MESA", stored))!!
            assertEquals(listOf("ME", "SA"), result.syllables.map { it.text })
        }
    }

    @Test fun `preserves punctuation and spaces as separators not pieces`() {
        val result = prepare(word("¡LA MESA!", listOf("LA", "ME", "SA")))!!
        assertEquals(listOf("¡", " ", ""), result.syllables.map { it.separatorBefore })
        assertEquals("!", result.trailingSeparator)
        assertTrue(result.letters.all { it.text.all(Char::isLetter) })
        assertEquals(" ", result.letters[2].separatorBefore)
        assertEquals("¡LA MESA!", result.syllables.joinToString("") { it.separatorBefore + it.text } + result.trailingSeparator)
    }

    @Test fun `does not accept a syllable that crosses a word separator`() {
        val result = prepare(word("LA MESA", listOf("LAME", "SA")))!!
        assertEquals(listOf("LA", "ME", "SA"), result.syllables.map { it.text })
    }

    @Test fun `normalizes decomposed accents while preserving spanish letters`() {
        val result = prepare(word("nin\u0303o, pingu\u0308ino", emptyList()))!!
        assertEquals("NIÑO, PINGÜINO", result.displayText)
        assertTrue(result.letters.any { it.text == "Ñ" })
        assertTrue(result.letters.any { it.text == "Ü" })
        assertEquals(result.displayText.filter(Char::isLetter), result.syllables.joinToString("") { it.text })
    }

    @Test fun `no playable letters never produces an empty winning board`() {
        listOf("", "  ", "?!", "123").forEach { assertNull(prepare(word(it, emptyList()))) }
    }
}
