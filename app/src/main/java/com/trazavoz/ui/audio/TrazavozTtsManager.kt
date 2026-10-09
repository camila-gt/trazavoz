package com.trazavoz.ui.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrazavozTtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener, GameAudio {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingUtterances = ConcurrentHashMap<String, CompletableDeferred<Unit>>()

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                tts?.setSpeechRate(0.80f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) = finish(utteranceId)
                    @Deprecated("Required by the TTS listener")
                    override fun onError(utteranceId: String?) = finish(utteranceId)
                    override fun onStop(utteranceId: String?, interrupted: Boolean) = finish(utteranceId)
                })
            }
        }
    }

    fun hablarPalabra(palabra: String) {
        if (!isInitialized) return
        tts?.speak(palabra, TextToSpeech.QUEUE_FLUSH, null, "palabra_$palabra")
    }

    fun hablarSilaba(silaba: String) {
        if (!isInitialized) return
        tts?.speak(silaba, TextToSpeech.QUEUE_FLUSH, null, "silaba_$silaba")
    }

    fun hablarLetra(letra: Char) {
        if (!isInitialized) return
        tts?.speak(letra.toString(), TextToSpeech.QUEUE_FLUSH, null, "letra_$letra")
    }

    /**
     * Pronuncia varias partes en orden sin que se pisen entre sí: la primera
     * corta lo que hubiera sonando (QUEUE_FLUSH) y las siguientes se encolan
     * (QUEUE_ADD). Útil para leer sílaba1, sílaba2 y luego la palabra completa.
     */
    fun hablarSecuencia(partes: List<String>) {
        if (!isInitialized) return
        partes.forEachIndexed { index, parte ->
            val modo = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(parte, modo, null, "seq_${index}_$parte")
        }
    }

    private fun finish(id: String?) {
        id?.let { pendingUtterances.remove(it)?.complete(Unit) }
    }

    override fun speakWord(text: String) = hablarPalabra(text)

    override fun speakPiece(text: String, isSyllable: Boolean) {
        if (isSyllable) hablarSilaba(text)
        else text.firstOrNull()?.let(::hablarLetra)
    }

    override suspend fun speakSequenceAndAwait(parts: List<String>) = withContext(Dispatchers.Main.immediate) {
        if (!isInitialized || parts.isEmpty()) return@withContext
        val id = UUID.randomUUID().toString()
        val completion = CompletableDeferred<Unit>()
        pendingUtterances[id] = completion
        try {
            for ((index, part) in parts.withIndex()) {
                val result = tts?.speak(
                    part,
                    if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                    null,
                    if (index == parts.lastIndex) id else "${id}_$index"
                )
                if (result != TextToSpeech.SUCCESS) {
                    stop()
                    return@withContext
                }
            }
            // Some engines never deliver onDone; do not lock the exercise indefinitely.
            if (withTimeoutOrNull(60_000) { completion.await(); true } == null) stop()
        } finally {
            pendingUtterances.remove(id)
        }
    }

    override fun stop() {
        tts?.stop()
        pendingUtterances.values.forEach { it.complete(Unit) }
        pendingUtterances.clear()
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        isInitialized = false
    }
}
