package com.trazavoz.ui.syllables

/** Fila a la que pertenece un hueco del whiteboard. */
enum class SlotRow { TUTOR, CHILD }

/** Resultado de validar un hueco del niño contra la columna del tutor. */
enum class SlotValidation { NONE, CORRECT, INCORRECT }

/** Sílaba del banco lateral. Es infinita: nunca se agota al arrastrarla. */
data class SyllableTile(val syllable: String)

/** Hueco donde se coloca una sílaba (fila tutor o fila niño). */
data class SyllableSlot(
    val id: String,
    val row: SlotRow,
    val index: Int,
    val syllable: String? = null,
    val validation: SlotValidation = SlotValidation.NONE
)

data class SyllablePracticeUiState(
    val letter: String = "",
    val bank: List<SyllableTile> = emptyList(),
    val tutorSlots: List<SyllableSlot> = emptyList(),
    val childSlots: List<SyllableSlot> = emptyList(),
    val showCelebration: Boolean = false
)

/** Número fijo de huecos por palabra en este módulo (spec §7). */
const val SYLLABLES_PER_WORD = 2

private val VOWELS = listOf('A', 'E', 'I', 'O', 'U')

/**
 * Genera el banco de 5 sílabas de una letra concatenando la consonante con cada
 * vocal (ej. "M" -> [MA, ME, MI, MO, MU]). Función pura y reutilizable.
 */
fun generarBancoSilabas(letter: String): List<String> {
    val consonante = letter.trim().uppercase()
    if (consonante.length != 1) return emptyList()
    return VOWELS.map { "$consonante$it" }
}

/** True si la letra puede formar un banco consonante+vocal (excluye vocales). */
fun esConsonante(letter: String): Boolean {
    val c = letter.trim().uppercase()
    return c.length == 1 && c[0].isLetter() && c[0] !in VOWELS
}
