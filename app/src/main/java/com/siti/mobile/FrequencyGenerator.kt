package com.siti.mobile

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import com.siti.mobile.mvvm.splash.view.TAG
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

class FrequencyGenerator {

   companion object {
       private val sampleRate = 44100

       private val freqStart = 17000
       private val freqEnd = 19000

       private val baseFreq = 17500  // Digit 0
       private val stepFreq = 500    // Steps by 500

       private val toneDurationMs = 1000
       private val fadeDurationMs = 1

       var audioTrack: AudioTrack? = null

       var called : Long = System.currentTimeMillis()



//    fun startTransmittingDigits(number: String) {
//        thread(start = true) {
//            while(true){
//                if (!number.matches(Regex("^[0-9]+$"))) return@thread
//
//                val frequencies = mutableListOf<Int>()
//                frequencies.add(freqStart)
//
//                number.forEach { digit ->
//                    val freq = baseFreq + (digit.digitToInt() * stepFreq)
//                    Log.w(TAG, "FREQ: $freq")
//                    frequencies.add(freq)
//                }
//
//                frequencies.add(freqEnd)
//
//                transmitFrequencies(frequencies)
//            }
//
//        }
//    }

       fun startTransmittingDigits(number: String, seconds: Long, calledMethod : Long) {
           audioTrack?.stop()
           audioTrack?.release()

           val minBufferSize = AudioTrack.getMinBufferSize(
               sampleRate,
               AudioFormat.CHANNEL_OUT_MONO,
               AudioFormat.ENCODING_PCM_FLOAT
           )

           audioTrack = AudioTrack.Builder()
                   .setAudioFormat(
                       AudioFormat.Builder()
                           .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                           .setSampleRate(sampleRate)
                           .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                           .build()
                   )
                   .setBufferSizeInBytes(minBufferSize)
                   .setTransferMode(AudioTrack.MODE_STREAM)
                   .setAudioAttributes(
                       android.media.AudioAttributes.Builder()
                           .setUsage(AudioManager.STREAM_MUSIC)
                           .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                           .build()
                   )
                   .build().apply {
                       play()
                   }

           thread(start = true) {
               Log.w(TAG, "Input: $number")  // Corregido: $String → $number
               val base3 = number.toIntOrNull()?.toString(3) ?: return@thread

               Log.w(TAG, "Base-3: $base3")

               val frequencies = mutableListOf<Int>()
               frequencies.add(freqStart)

               base3.forEach { digit ->
                   val freq = baseFreq + (digit.digitToInt() * stepFreq)
                   Log.w(TAG, "FREQ: $freq")
                   frequencies.add(freq)
               }

               frequencies.add(freqEnd)

               val startTime = System.currentTimeMillis()
               val durationMillis = seconds * 1000

               while (System.currentTimeMillis() - startTime < durationMillis && calledMethod == called) {
                   transmitFrequencies(frequencies)
                   Thread.sleep(2000) // puedes ajustar esto según tu tiempo por transmisión
               }
           }
       }




//    private fun transmitFrequencies(frequencies: List<Int>) {
//        val samplesPerTone = (sampleRate * toneDurationMs / 1000.0).toInt()
//        val totalSamples = samplesPerTone * frequencies.size
//        val samples = FloatArray(totalSamples)
//
//        for ((index, freq) in frequencies.withIndex()) {
//            val tone = generateToneWithFade(freq, samplesPerTone)
//            System.arraycopy(tone, 0, samples, index * samplesPerTone, samplesPerTone)
//        }
//
//        audioTrack.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
//    }

       private fun transmitFrequencies(frequencies: List<Int>) {
           val samplesPerTone = (sampleRate * toneDurationMs / 1000.0).toInt()
           val gapDurationMs = 200  // silence between tones
           val samplesPerGap = (sampleRate * gapDurationMs / 1000.0).toInt()

           val totalSamples = (samplesPerTone + samplesPerGap) * frequencies.size
           val samples = FloatArray(totalSamples)

           for ((index, freq) in frequencies.withIndex()) {
               val tone = generateToneWithFade(freq, samplesPerTone)
               val gap = FloatArray(samplesPerGap) { 0.0f } // silent part

               // calculate where to paste
               val startIndex = index * (samplesPerTone + samplesPerGap)

               // paste tone
               System.arraycopy(tone, 0, samples, startIndex, samplesPerTone)

               // paste gap
               System.arraycopy(gap, 0, samples, startIndex + samplesPerTone, samplesPerGap)
           }

           audioTrack?.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
       }


       private fun generateToneWithFade(frequency: Int, samplesCount: Int): FloatArray {
           val fadeSamples = (sampleRate * fadeDurationMs / 1000.0).toInt()
           val angularFreq = 2.0 * PI * frequency / sampleRate
           val samples = FloatArray(samplesCount)

           for (i in 0 until samplesCount) {
               val envelope = when {
                   i < fadeSamples -> i.toDouble() / fadeSamples
                   i >= samplesCount - fadeSamples -> (samplesCount - i).toDouble() / fadeSamples
                   else -> 1.0
               }
               val volume = 1.0 // 0.5 half volume, 0.0 mute 1.0 full volume
               samples[i] = (sin(i * angularFreq) * envelope * volume).toFloat()
           }

           return samples
       }
   }


}
