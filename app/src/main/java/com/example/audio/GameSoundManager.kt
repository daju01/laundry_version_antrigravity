package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural Audio Synthesizer & Sound Effect Engine for Laundry Tycoon.
 * Zero external audio files required: synthesizes pure, studio-quality 16-bit PCM waveforms
 * directly in memory and plays them through Android AudioTrack.
 */
object GameSoundManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val audioTrackSemaphore = Semaphore(4)
    private const val SAMPLE_RATE = 44100

    @Volatile
    var isSoundEnabled: Boolean = true

    @Volatile
    var isHapticEnabled: Boolean = true

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Triumphant Level Up / Capacity Upgrade Fanfare:
     * Dynamic rising arpeggio (C5 -> E5 -> G5 -> C6) culminating in a sparkling grand chord
     * with warm harmonics and exponential decay. Gives a visceral sense of accomplishment!
     */
    fun playCapacityUpgradeFanfare() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                // Total duration ~1.4 seconds
                val totalDuration = 1.4f
                val totalSamples = (SAMPLE_RATE * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)

                // Define note progression: (freq, startTimeSec, durationSec, volume)
                data class Note(val freq: Float, val startSec: Float, val durSec: Float, val gain: Float)
                val notes = listOf(
                    Note(523.25f, 0.00f, 0.22f, 0.55f), // C5
                    Note(659.25f, 0.16f, 0.24f, 0.60f), // E5
                    Note(783.99f, 0.32f, 0.26f, 0.65f), // G5
                    Note(1046.50f, 0.48f, 0.35f, 0.75f), // C6
                    // Triumphant Grand Sustained Chord at 0.65s:
                    Note(523.25f, 0.65f, 0.75f, 0.40f), // C5 root
                    Note(659.25f, 0.65f, 0.75f, 0.40f), // E5 third
                    Note(783.99f, 0.65f, 0.75f, 0.45f), // G5 fifth
                    Note(1046.50f, 0.65f, 0.75f, 0.60f), // C6 octave
                    Note(1318.51f, 0.72f, 0.68f, 0.45f)  // E6 sparkling high flourish
                )

                for (n in notes) {
                    val startIdx = (n.startSec * SAMPLE_RATE).toInt()
                    val noteSamples = (n.durSec * SAMPLE_RATE).toInt()
                    val endIdx = (startIdx + noteSamples).coerceAtMost(totalSamples)

                    for (i in startIdx until endIdx) {
                        val t = (i - startIdx).toFloat() / SAMPLE_RATE
                        val progress = (i - startIdx).toFloat() / noteSamples

                        // Attack-Decay Envelope (fast 15ms attack, smooth exponential decay)
                        val attack = (t / 0.015f).coerceIn(0f, 1f)
                        val decay = exp(-3.2f * progress)
                        val env = attack * decay

                        // Waveform with 2nd and 3rd harmonics for rich, musical game console timbre
                        val fundamental = sin(2.0 * PI * n.freq * t).toFloat()
                        val harmonic2 = 0.3f * sin(2.0 * PI * (n.freq * 2) * t).toFloat()
                        val harmonic3 = 0.15f * sin(2.0 * PI * (n.freq * 3) * t).toFloat()

                        val sampleValue = (fundamental + harmonic2 + harmonic3) * env * n.gain
                        val currentShort = buffer[i]
                        val mixed = (currentShort + (sampleValue * 16000).toInt()).coerceIn(-32767, 32767)
                        buffer[i] = mixed.toShort()
                    }
                }

                triggerHapticPattern(longArrayOf(0, 70, 60, 90, 80, 200))
                playBuffer(buffer)
            } catch (_: Throwable) {
                // Audio gracefully handled
            }
        }
    }

    /**
     * Dynamic 3D expansion whoosh effect:
     * A smooth ascending resonant frequency sweep (160 Hz -> 850 Hz) with soft air texture,
     * representing 3D room expansion and physical capacity surge!
     */
    fun play3DExpansionWhoosh() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durSec = 0.65f
                val totalSamples = (SAMPLE_RATE * durSec).toInt()
                val buffer = ShortArray(totalSamples)

                var phase = 0.0
                for (i in 0 until totalSamples) {
                    val progress = i.toFloat() / totalSamples
                    // Rising sweep from 160 Hz to 850 Hz
                    val currentFreq = 160.0 + (690.0 * progress * progress)
                    val dt = 1.0 / SAMPLE_RATE
                    phase += 2.0 * PI * currentFreq * dt

                    // Envelope: swell up and fade
                    val env = (sin(PI * progress) * (1f - progress * 0.3f)).toFloat()
                    val wave = (sin(phase) + 0.25 * sin(phase * 1.5)).toFloat()

                    buffer[i] = (wave * env * 14000f).toInt().coerceIn(-32767, 32767).toShort()
                }

                triggerHapticPattern(longArrayOf(0, 40, 50, 80))
                playBuffer(buffer)
            } catch (_: Throwable) {
                // Graceful fallback
            }
        }
    }

    /**
     * Sparkly chime for streak achievements and visual satisfaction bonus claim!
     */
    fun playStreakBonusChime() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durSec = 0.7f
                val totalSamples = (SAMPLE_RATE * durSec).toInt()
                val buffer = ShortArray(totalSamples)

                // High twin bell: E6 (1318 Hz) and B6 (1975 Hz)
                val notes = listOf(
                    Triple(1318.5f, 0.00f, 0.45f),
                    Triple(1975.5f, 0.12f, 0.55f),
                    Triple(2637.0f, 0.22f, 0.48f)
                )

                for ((freq, startSec, dur) in notes) {
                    val startIdx = (startSec * SAMPLE_RATE).toInt()
                    val len = (dur * SAMPLE_RATE).toInt()
                    val endIdx = (startIdx + len).coerceAtMost(totalSamples)

                    for (i in startIdx until endIdx) {
                        val t = (i - startIdx).toFloat() / SAMPLE_RATE
                        val prog = (i - startIdx).toFloat() / len
                        val env = exp(-5.0f * prog)
                        val sample = (sin(2.0 * PI * freq * t) + 0.2 * sin(4.0 * PI * freq * t)).toFloat() * env * 0.4f
                        val current = buffer[i]
                        buffer[i] = (current + (sample * 20000).toInt()).coerceIn(-32767, 32767).toShort()
                    }
                }

                triggerHapticPattern(longArrayOf(0, 40, 50, 60))
                playBuffer(buffer)
            } catch (_: Throwable) {
                // Graceful fallback
            }
        }
    }

    /**
     * Ka-ching coin reward sound for task completion & cash payouts.
     */
    fun playKaChing() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durSec = 0.4f
                val totalSamples = (SAMPLE_RATE * durSec).toInt()
                val buffer = ShortArray(totalSamples)

                val notes = listOf(
                    Pair(987.7f, 0.00f),  // B5
                    Pair(1318.5f, 0.08f)  // E6
                )
                for ((freq, startSec) in notes) {
                    val startIdx = (startSec * SAMPLE_RATE).toInt()
                    val len = (0.28f * SAMPLE_RATE).toInt()
                    val endIdx = (startIdx + len).coerceAtMost(totalSamples)
                    for (i in startIdx until endIdx) {
                        val t = (i - startIdx).toFloat() / SAMPLE_RATE
                        val prog = (i - startIdx).toFloat() / len
                        val env = exp(-7.0f * prog)
                        val sample = sin(2.0 * PI * freq * t).toFloat() * env * 0.45f
                        val current = buffer[i]
                        buffer[i] = (current + (sample * 18000).toInt()).coerceIn(-32767, 32767).toShort()
                    }
                }

                playBuffer(buffer)
            } catch (_: Throwable) {
                // Graceful fallback
            }
        }
    }

    /**
     * Crisp tactile pop sound for station tap boost interactions.
     */
    fun playTapBoost() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durSec = 0.14f
                val totalSamples = (SAMPLE_RATE * durSec).toInt()
                val buffer = ShortArray(totalSamples)

                for (i in 0 until totalSamples) {
                    val t = i.toFloat() / SAMPLE_RATE
                    val prog = i.toFloat() / totalSamples
                    val freq = 580f + (320f * prog)
                    val env = exp(-12.0f * prog)
                    val sample = sin(2.0 * PI * freq * t).toFloat() * env * 0.35f
                    buffer[i] = (sample * 16000).toInt().coerceIn(-32767, 32767).toShort()
                }

                triggerHapticPattern(longArrayOf(0, 20))
                playBuffer(buffer)
            } catch (_: Throwable) {
                // Graceful fallback
            }
        }
    }

    private suspend fun playBuffer(buffer: ShortArray) {
        audioTrackSemaphore.withPermit {
            var track: AudioTrack? = null
            try {
                val bufferSize = buffer.size * 2
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val format = AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                track = AudioTrack.Builder()
                    .setAudioAttributes(attributes)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()

                // Non-blocking coroutine suspension instead of blocking Thread.sleep
                val sleepMs = ((buffer.size.toFloat() / SAMPLE_RATE) * 1000).toLong() + 80L
                delay(sleepMs)
            } catch (_: Throwable) {
                // Audio output device handled safely
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Throwable) {}
            }
        }
    }

    private fun triggerHapticPattern(timings: LongArray) {
        if (!isHapticEnabled) return
        val ctx = appContext ?: return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(timings, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(timings, -1)
                }
            }
        } catch (_: Throwable) {
            // Haptic handled safely
        }
    }
}
