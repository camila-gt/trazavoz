package com.trazavoz.ui.syllables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trazavoz.ui.audio.TrazavozTtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyllablePracticeViewModel @Inject constructor(
    private val ttsManager: TrazavozTtsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyllablePracticeUiState())
    val uiState: StateFlow<SyllablePracticeUiState> = _uiState.asStateFlow()

    /** Prepara el whiteboard para una letra. Idempotente: solo reinicia si cambia. */
    fun init(letter: String) {
        if (_uiState.value.letter == letter && _uiState.value.bank.isNotEmpty()) return

        val bank = generarBancoSilabas(letter).map { SyllableTile(it) }
        _uiState.value = SyllablePracticeUiState(
            letter = letter,
            bank = bank,
            tutorSlots = crearSlots(SlotRow.TUTOR),
            childSlots = crearSlots(SlotRow.CHILD)
        )
    }

    /** Coloca (o reemplaza) una sílaba en un hueco del tutor y la pronuncia. */
    fun onDropOnTutor(index: Int, syllable: String) {
        _uiState.update { state ->
            state.copy(tutorSlots = state.tutorSlots.map { slot ->
                if (slot.index == index) slot.copy(syllable = syllable) else slot
            })
        }
        ttsManager.hablarSilaba(syllable)
    }

    /**
     * Coloca (o reemplaza) una sílaba en un hueco del niño, la valida contra la
     * columna del tutor, la pronuncia y comprueba si la fila quedó completa.
     */
    fun onDropOnChild(index: Int, syllable: String) {
        val expected = _uiState.value.tutorSlots.getOrNull(index)?.syllable
        val validation = when {
            expected == null -> SlotValidation.NONE
            expected == syllable -> SlotValidation.CORRECT
            else -> SlotValidation.INCORRECT
        }

        _uiState.update { state ->
            state.copy(childSlots = state.childSlots.map { slot ->
                if (slot.index == index) slot.copy(syllable = syllable, validation = validation) else slot
            })
        }
        ttsManager.hablarSilaba(syllable)
        checkChildCompletion()
    }

    /** Al tocar una sílaba que colocó el niño, la vuelve a pronunciar. */
    fun onChildSlotTap(index: Int) {
        _uiState.value.childSlots.getOrNull(index)?.syllable?.let { ttsManager.hablarSilaba(it) }
    }

    /** Lee la palabra completa formada por la fila del tutor (si está llena). */
    fun speakTutorWord() {
        val palabra = palabraDe(_uiState.value.tutorSlots) ?: return
        ttsManager.hablarPalabra(palabra)
    }

    /** 🎲 Genera una palabra aleatoria de 2 sílabas del banco en la fila del tutor. */
    fun onRandom() {
        val bank = _uiState.value.bank
        if (bank.isEmpty()) return

        _uiState.update { state ->
            state.copy(tutorSlots = state.tutorSlots.map { slot ->
                slot.copy(syllable = bank.random().syllable)
            })
        }
        speakTutorWord()
    }

    /** 🔄 Vacía las filas tutor y niño; el banco permanece intacto. */
    fun onClearAll() {
        _uiState.update { state ->
            state.copy(
                tutorSlots = crearSlots(SlotRow.TUTOR),
                childSlots = crearSlots(SlotRow.CHILD),
                showCelebration = false
            )
        }
    }

    private fun checkChildCompletion() {
        val childSlots = _uiState.value.childSlots
        if (childSlots.any { it.syllable == null }) return

        // Solo se celebra si TODAS las sílabas del niño son correctas (verde).
        if (childSlots.any { it.validation != SlotValidation.CORRECT }) return

        val silabas = childSlots.mapNotNull { it.syllable }
        val palabra = silabas.joinToString("").lowercase()
        ttsManager.hablarSecuencia(silabas + palabra)

        _uiState.update { it.copy(showCelebration = true) }
        viewModelScope.launch {
            delay(CELEBRATION_DURATION_MS)
            _uiState.update { it.copy(showCelebration = false) }
        }
    }

    private fun crearSlots(row: SlotRow): List<SyllableSlot> {
        val prefix = if (row == SlotRow.TUTOR) "tutor" else "child"
        return (0 until SYLLABLES_PER_WORD).map { i ->
            SyllableSlot(id = "$prefix-$i", row = row, index = i)
        }
    }

    private fun palabraDe(slots: List<SyllableSlot>): String? {
        if (slots.any { it.syllable == null }) return null
        return slots.mapNotNull { it.syllable }.joinToString("").lowercase()
    }

    private companion object {
        const val CELEBRATION_DURATION_MS = 5000L
    }
}
