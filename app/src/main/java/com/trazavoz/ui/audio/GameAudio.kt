package com.trazavoz.ui.audio

/** Small audio boundary so game timing can be tested without Android's TTS engine. */
interface GameAudio {
    fun speakWord(text: String)
    fun speakPiece(text: String, isSyllable: Boolean)
    suspend fun speakSequenceAndAwait(parts: List<String>)
    fun stop()
}
