package com.trazavoz.ui.game

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class GameStyleTest {
    @Test fun `all spanish vowels use canonical red including accents and diaeresis`() {
        val text = "AEIOUÁÉÍÓÚÜaeiouáéíóúü"
        val styled = buildPieceAnnotatedString(text)
        assertEquals(text, styled.text)
        assertEquals(Color(0xFFC92A2A), VowelColor)
        styled.spanStyles.forEach { assertEquals(VowelColor, it.item.color) }
    }

    @Test fun `consonants including enye use canonical blue in mixed syllables`() {
        val styled = buildPieceAnnotatedString("MÉÑÜ")
        assertEquals(Color(0xFF1E6091), ConsonantColor)
        assertEquals(listOf(ConsonantColor, VowelColor, ConsonantColor, VowelColor), styled.spanStyles.map { it.item.color })
    }
}
