package com.gymhelper.app.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

class GongPlayer(context: Context) {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)

    fun playGong() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800)
    }

    fun release() {
        toneGenerator.release()
    }
}
