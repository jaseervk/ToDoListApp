package com.todoapp.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages a [SoundPool] with lightweight sound effects for UI feedback.
 *
 * Call [release] when the app goes background (handled by the lifecycle observer
 * wired in [MainActivity]).
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(audioAttributes)
        .build()

    private var soundIdAdd     = 0
    private var soundIdComplete = 0
    private var soundIdDelete  = 0
    private var soundIdSplash  = 0
    private var loaded = false

    init {
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded = true
        }
        soundIdAdd = loadSoundByName("sound_add")
        soundIdComplete = loadSoundByName("sound_complete")
        soundIdDelete = loadSoundByName("sound_delete")
        soundIdSplash = loadSoundByName("sound_splash")
    }

    fun playAdd()      = play(soundIdAdd)
    fun playComplete() = play(soundIdComplete)
    fun playDelete()   = play(soundIdDelete)
    fun playSplash()   = play(soundIdSplash)

    private fun play(soundId: Int) {
        if (loaded && soundId != 0) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
        }
    }

    private fun loadSoundByName(fileName: String): Int {
        val resId = context.resources.getIdentifier(fileName, "raw", context.packageName)
        return if (resId != 0) soundPool.load(context, resId, 1) else 0
    }

    /** Call from Activity.onDestroy to free native resources. */
    fun release() {
        soundPool.release()
    }
}
