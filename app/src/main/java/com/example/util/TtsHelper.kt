package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isInitialized = true
        }
    }

    fun speak(text: String, languageCode: String = "en") {
        if (!isInitialized) return
        val locale = when (languageCode) {
            "ta" -> Locale("ta", "IN")
            "hi" -> Locale("hi", "IN")
            "ml" -> Locale("ml", "IN")
            "te" -> Locale("te", "IN")
            "kn" -> Locale("kn", "IN")
            else -> Locale.US
        }
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PlantDoctorTTS")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
