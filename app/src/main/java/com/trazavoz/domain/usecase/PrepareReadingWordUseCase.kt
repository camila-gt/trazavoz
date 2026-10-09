package com.trazavoz.domain.usecase

import com.trazavoz.domain.model.Word
import java.text.Normalizer
import java.util.Locale
import javax.inject.Inject

data class ReadingUnit(val text: String, val separatorBefore: String = "")

data class ReadingWord(
    val displayText: String,
    val syllables: List<ReadingUnit>,
    val letters: List<ReadingUnit>,
    val trailingSeparator: String
)

/** Preserves spelling and separators while excluding them from draggable pieces. */
class PrepareReadingWordUseCase @Inject constructor(
    private val splitSyllables: SplitSyllablesUseCase
) {
    operator fun invoke(word: Word): ReadingWord? {
        val text = normalize(word.text).trim()
        if (text.none(Char::isLetter)) return null
        val letters = align(text, text.filter(Char::isLetter).map(Char::toString)) ?: return null
        val stored = word.syllables.map { normalize(it).trim() }
        val syllables = align(text, stored) ?: align(
            text,
            Regex("\\p{L}+").findAll(text).flatMap { match ->
                splitSyllables(match.value).asSequence().map(::normalize)
            }.toList()
        ) ?: return null
        val trailing = text.takeLastWhile { !it.isLetter() }
        return ReadingWord(text, syllables, letters, trailing)
    }

    private fun align(text: String, pieces: List<String>): List<ReadingUnit>? {
        if (pieces.isEmpty() || pieces.any { it.isEmpty() || !it.all(Char::isLetter) }) return null
        var cursor = 0
        val result = pieces.map { piece ->
            val start = cursor
            while (cursor < text.length && !text[cursor].isLetter()) cursor++
            val separator = text.substring(start, cursor)
            if (!text.startsWith(piece, cursor)) return null
            cursor += piece.length
            ReadingUnit(piece, separator)
        }
        if (text.substring(cursor).any(Char::isLetter)) return null
        return result
    }

    private fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFC).uppercase(Locale.ROOT)
}
