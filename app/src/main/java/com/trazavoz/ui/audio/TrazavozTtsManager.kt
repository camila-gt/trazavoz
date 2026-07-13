package com.trazavoz.ui.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrazavozTtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                tts?.setSpeechRate(0.80f)
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

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        isInitialized = false
    }
}
