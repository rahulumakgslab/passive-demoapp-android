package com.sonde.mentalfitness.presentation.ui.textIndependent

import android.content.ContentValues.TAG
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.*
import android.media.MediaRecorder.AudioSource.UNPROCESSED
import android.media.MediaRecorder.AudioSource.VOICE_RECOGNITION
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Mic audio recorder.
 *
 * @exception IllegalArgumentException if a sample rate is unsupported.
 */
class MicAudioRecorder @Throws(IllegalArgumentException::class) constructor(
    val sampleRate: Int,
    // 32 ms is required minimum for most VoiceSDK operation
    minAudioLengthInByteChunkInMs: Int = 32
) : Iterator<ByteArray> {

    val encoding
        get() = RECORDER_AUDIO_ENCODING

    var bufferSize: Int

    private var recorder: AudioRecord?

    private var recordingThread: Thread? = null
    private var minBufferSizeByAudioLength = 0
    private val audioChunkDeque = ConcurrentLinkedDeque<ByteArray>()

    var isPaused: Boolean = false
        private set

    init {

        minBufferSizeByAudioLength = getAudioSize(minAudioLengthInByteChunkInMs.toLong(), sampleRate)

        bufferSize = getMinBufferSize(
            sampleRate,
            RECORDER_CHANNELS,
            RECORDER_AUDIO_ENCODING
        )

        // If min buffer size is greater than required by user than write a log about it but doesn't break work of
        // audio recorder.
        if (bufferSize > minBufferSizeByAudioLength) {
            Log.w(TAG, "Min buffer size is greater than a min buffer size required by user!")
            minBufferSizeByAudioLength = bufferSize
        }

        recorder = createMediaAudioRecord()
    }

    /**
     * Function for start an audio recording.
     *
     * @exception IllegalStateException if something wrong when try to start an [MicAudioRecorder]
     */
    fun startAudioRecording() {
        stopAudioRecording()

        recordingThread = thread(true) {

            if ((recorder == null) || (recorder!!.state == STATE_UNINITIALIZED)) {
                recorder = createMediaAudioRecord()
            }

            if (recorder!!.state == STATE_INITIALIZED) {
                try {
                    recorder?.startRecording()

                    // Init read buffers
                    val bufferToReadBytes = ByteArray(bufferSize)
                    val byteBuffer = ByteBuffer.allocate(minBufferSizeByAudioLength * 2)

                    while ((recorder != null) && (recorder!!.recordingState == RECORDSTATE_RECORDING)) {

                        // Check on a paused state
                        if (isPaused) {
                            continue
                        }

                        val numReadBytes = recorder!!.read(bufferToReadBytes, 0, bufferSize)

                        if (numReadBytes == 0) {
                            continue
                        }

                        if (numReadBytes > 0) {
                            // Write bytes to byteBuffer for keeping until will be minBufferSizeByAudioLength bytes
                            // into byteBuffer
                            byteBuffer.put(bufferToReadBytes.copyOfRange(0, numReadBytes))

                            // Get position of byte buffer
                            val byteBufferPosition = byteBuffer.position()

                            if (byteBufferPosition >= minBufferSizeByAudioLength) {
                                val bufferToSendBytes = ByteArray(byteBufferPosition)

                                // Copy bytes to send
                                byteBuffer.position(0)
                                byteBuffer.get(bufferToSendBytes)
                                byteBuffer.clear()

                                // Add bytes
                                try {
                                    audioChunkDeque.addLast(bufferToSendBytes)
                                } catch (e: java.lang.IllegalStateException) {
                                    // If the element cannot be added at this time due to capacity restrictions
                                    // then clear a deque
                                    audioChunkDeque.clear()
                                    continue
                                } catch (e: OutOfMemoryError) {
                                    // If the element cannot be added at this time due to memory restrictions
                                    // then clear a deque
                                    audioChunkDeque.clear()
                                    continue
                                }
                            }
                        } else {
                            val messageOfException = when (numReadBytes) {
                                ERROR_INVALID_OPERATION -> "Audio recorder isn't properly initialized"
                                ERROR_BAD_VALUE -> "The parameters don't resolve to valid data and indexes"
                                ERROR_DEAD_OBJECT -> "Try read audio data from dead Audio recorder's object"
                                ERROR -> "Get error when read audio data"
                                else -> "Unknown error code: $numReadBytes"
                            }

                            val audioRecordingException = IllegalStateException(messageOfException)
                            Log.e(TAG, messageOfException, audioRecordingException)
                            error(audioRecordingException)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Something wrong when audio recording", e)
                    error(e)
                }
            } else {
                val messageOfException = "Audio recorder isn't properly initialized"
                val audioRecordingException = IllegalStateException(messageOfException)
                Log.e(TAG, messageOfException, audioRecordingException)
                error(audioRecordingException)
            }
        }

        // Wait when recorder is started
        runBlocking {
            withTimeout(1000) {
                Log.d(TAG, "Start wait when recorder is stared")
                while (recordingThread?.isAlive == true && (recorder?.recordingState != RECORDSTATE_RECORDING)) {
                    delay(1)
                }
                Log.d(TAG, "Finish wait when recorder is stared")
            }
        }
    }

    /**
     * Set on pause audio recorder.
     */
    fun pauseAudioRecording() {
        isPaused = true
    }

    /**
     * Resume audio recording.
     */
    fun resumeAudioRecording() {
        isPaused = false
    }

    /**
     * Returns true if audio recorder is stopped and false otherwise.
     */
    fun isStopped() = if (recorder == null) true else (recorder?.recordingState == RECORDSTATE_STOPPED)

    /**
     * Stop an audio recording.
     *
     * @exception IllegalStateException if something wrong when try to stop an [MicAudioRecorder]
     */
    @Throws(IllegalStateException::class)
    fun stopAudioRecording() {
        // Stop recording
        if (recorder?.recordingState != RECORDSTATE_STOPPED) {
            recorder?.stop()
        }

        // Clear resources
        recordingThread?.join()
        recordingThread = null

        recorder?.release()
        recorder = null

        audioChunkDeque.clear()

        // Set a pause flag as false
        isPaused = false
    }

    @Throws(IllegalArgumentException::class)
    private fun createMediaAudioRecord(): AudioRecord {
        try {

            val audioSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                UNPROCESSED
            } else {
                VOICE_RECOGNITION
            }

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val format = AudioFormat.Builder()
                    .setEncoding(RECORDER_AUDIO_ENCODING)
                    .setSampleRate(sampleRate)
                    .setChannelMask(RECORDER_CHANNELS)
                    .build()

                val builder = Builder()
                    .setAudioSource(audioSource)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(bufferSize)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Mark audio source as privacy
                    builder.setPrivacySensitive(true)
                }

                builder.build()
            } else {
                AudioRecord(
                    audioSource,
                    sampleRate,
                    RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING,
                    bufferSize
                )
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "An error occurred during the creation of the audio record", e)
            throw e
        }
    }

    override fun hasNext(): Boolean {
        return audioChunkDeque.isNotEmpty()
    }

    override fun next(): ByteArray {
        return audioChunkDeque.removeFirst()
    }

    companion object {
        private const val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
        private const val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT

        /**
         * Calculating the average amplitude of the passed byte array with PCM16 encoding
         *
         * @param byteArray pass byte array
         * @return average amplitude in range 0...32767
         */
        fun averageAmplitudeEncodedPcm16(byteArray: ByteArray): Int {

            val numBytesInSample = 2
            var averageAmplitude = 0
            var iter = 0
            while (iter < byteArray.size / numBytesInSample) {

                val firstByte = byteArray[iter * numBytesInSample].toInt()
                val secondByte = byteArray[iter * numBytesInSample + 1].toInt()

                // Signed Int is calculated from two bytes
                averageAmplitude += abs(((firstByte shl 8) or secondByte))

                iter++
            }

            if (iter == 0) {
                return 0
            }

            return averageAmplitude / iter
        }

        /**
         * Calculating the norm average amplitude of the passed byte array with PCM16 encoding
         *
         * @param byteArray pass byte array
         * @return norm average amplitude in range 0...1
         */
        fun normAverageAmplitudeEncodedPcm16(byteArray: ByteArray): Float {
            return averageAmplitudeEncodedPcm16(byteArray).toFloat() / Short.MAX_VALUE
        }

        /**
         * Get audio duration in seconds of number bytes (PCM16 only)
         *
         * @param numberBytes
         * @param sampleRate
         * @return audio duration in ms
         */
        fun getAudioDurationInMs(numberBytes: Int, sampleRate: Int): Float {
            val bytePerSecond = sampleRate * 2
            return ((numberBytes.toFloat() / bytePerSecond) * 1000)
        }

        /**
         * Get audio size in bytes from duration in seconds (PCM16 only)
         *
         * @param durationInMs
         * @param sampleRate
         * @return audio size in bytes
         */
        fun getAudioSize(durationInMs: Long, sampleRate: Int): Int {
            return getAudioSize(durationInMs.toFloat(), sampleRate)
        }

        /**
         * Get audio size in bytes from duration in seconds (PCM16 only)
         *
         * @param durationInMs
         * @param sampleRate
         * @return audio size in bytes
         */
        fun getAudioSize(durationInMs: Float, sampleRate: Int): Int {
            val bytePerSecond = sampleRate * 2
            return ceil(durationInMs / 1000f * bytePerSecond).toInt()
        }
    }
}
