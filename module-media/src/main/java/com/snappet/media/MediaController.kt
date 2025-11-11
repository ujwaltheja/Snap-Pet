package com.snappet.media

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchShifter
import com.snappet.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Controller for audio recording and playback with pitch/tempo effects.
 */
class MediaController(
    private val context: Context,
    private val logger: Logger
) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingFile: File? = null

    companion object {
        private const val TAG = "MediaController"
        private const val SAMPLE_RATE = 22050
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val MAX_RECORDING_DURATION_MS = 8000L // 8 seconds max
    }

    /**
     * Start recording audio.
     */
    suspend fun startRecording(): RecordingResult = withContext(Dispatchers.IO) {
        if (isRecording) {
            return@withContext RecordingResult.AlreadyRecording
        }

        try {
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                logger.error(TAG, "Invalid buffer size: $bufferSize")
                return@withContext RecordingResult.Error("Invalid buffer size")
            }

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                logger.error(TAG, "AudioRecord not initialized")
                return@withContext RecordingResult.Error("AudioRecord initialization failed")
            }

            // Create temporary file
            recordingFile = File(context.cacheDir, "temp_recording_${System.currentTimeMillis()}.pcm")

            audioRecord?.startRecording()
            isRecording = true

            // Record audio in background
            val buffer = ShortArray(bufferSize / 2)
            val outputStream = FileOutputStream(recordingFile)

            var recordedDuration = 0L
            val startTime = System.currentTimeMillis()

            while (isRecording && recordedDuration < MAX_RECORDING_DURATION_MS) {
                val readResult = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readResult > 0) {
                    // Convert shorts to bytes
                    val byteBuffer = ByteArray(readResult * 2)
                    for (i in 0 until readResult) {
                        byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                        byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
                    }
                    outputStream.write(byteBuffer)
                }
                recordedDuration = System.currentTimeMillis() - startTime
            }

            outputStream.close()
            stopRecording()

            logger.info(TAG, "Recording completed: ${recordingFile?.absolutePath}")
            RecordingResult.Success(recordingFile!!)
        } catch (e: Exception) {
            logger.error(TAG, "Recording failed", e)
            stopRecording()
            RecordingResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Stop recording audio.
     */
    fun stopRecording() {
        if (!isRecording) return

        try {
            isRecording = false
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            logger.info(TAG, "Recording stopped")
        } catch (e: Exception) {
            logger.error(TAG, "Error stopping recording", e)
        }
    }

    /**
     * Play recorded audio with pitch shift effect.
     */
    suspend fun playWithEffect(file: File, pitchShift: Float = 1.0f): PlaybackResult =
        withContext(Dispatchers.IO) {
            if (!file.exists()) {
                return@withContext PlaybackResult.Error("File not found")
            }

            try {
                // For simplicity, we'll use a basic approach
                // In production, TarsosDSP can be used for more sophisticated effects
                playRawAudio(file, pitchShift)
                logger.info(TAG, "Playback completed with pitch shift: $pitchShift")
                PlaybackResult.Success
            } catch (e: Exception) {
                logger.error(TAG, "Playback failed", e)
                PlaybackResult.Error(e.message ?: "Unknown error")
            }
        }

    /**
     * Play raw PCM audio with basic pitch shifting.
     */
    private fun playRawAudio(file: File, pitchShift: Float) {
        val fileInputStream = FileInputStream(file)
        val audioData = fileInputStream.readBytes()
        fileInputStream.close()

        // Convert to short array
        val shortArray = ShortArray(audioData.size / 2)
        for (i in shortArray.indices) {
            shortArray[i] = ((audioData[i * 2 + 1].toInt() shl 8) or (audioData[i * 2].toInt() and 0xFF)).toShort()
        }

        // Apply basic pitch shift by sample rate adjustment
        val adjustedSampleRate = (SAMPLE_RATE * pitchShift).toInt().coerceIn(8000, 48000)

        val bufferSize = AudioTrack.getMinBufferSize(
            adjustedSampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AUDIO_FORMAT
        )

        val audioTrack = AudioTrack.Builder()
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(adjustedSampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AUDIO_FORMAT)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()

        audioTrack.play()
        audioTrack.write(shortArray, 0, shortArray.size)
        audioTrack.stop()
        audioTrack.release()
    }

    /**
     * Get the current recording file if exists.
     */
    fun getCurrentRecordingFile(): File? = recordingFile

    /**
     * Clean up temporary files.
     */
    fun cleanup() {
        recordingFile?.delete()
        recordingFile = null
    }

    sealed class RecordingResult {
        data class Success(val file: File) : RecordingResult()
        object AlreadyRecording : RecordingResult()
        data class Error(val message: String) : RecordingResult()
    }

    sealed class PlaybackResult {
        object Success : PlaybackResult()
        data class Error(val message: String) : PlaybackResult()
    }
}
