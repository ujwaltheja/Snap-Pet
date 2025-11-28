package com.snappet.utils

import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Local event logger that writes events to memory and optionally to a file.
 * No external telemetry or network calls.
 */
class Logger(private val logDir: File? = null) {
    private val events = ConcurrentLinkedQueue<LogEvent>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    companion object {
        private const val TAG = "SnapPet"
        private const val MAX_EVENTS_IN_MEMORY = 500

        @Volatile
        private var instance: Logger? = null

        fun getInstance(logDir: File? = null): Logger {
            return instance ?: synchronized(this) {
                instance ?: Logger(logDir).also { instance = it }
            }
        }
    }

    data class LogEvent(
        val timestamp: Long,
        val level: Level,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    ) {
        enum class Level { DEBUG, INFO, WARN, ERROR }
    }

    fun debug(tag: String, message: String) = log(LogEvent.Level.DEBUG, tag, message)
    fun info(tag: String, message: String) = log(LogEvent.Level.INFO, tag, message)
    fun warn(tag: String, message: String, throwable: Throwable? = null) =
        log(LogEvent.Level.WARN, tag, message, throwable)
    fun error(tag: String, message: String, throwable: Throwable? = null) =
        log(LogEvent.Level.ERROR, tag, message, throwable)

    private fun log(level: LogEvent.Level, tag: String, message: String, throwable: Throwable? = null) {
        val event = LogEvent(System.currentTimeMillis(), level, tag, message, throwable)
        events.add(event)

        // Trim old events
        while (events.size > MAX_EVENTS_IN_MEMORY) {
            events.poll()
        }

        // Log to Android logcat
        val fullTag = "$TAG/$tag"
        when (level) {
            LogEvent.Level.DEBUG -> Log.d(fullTag, message, throwable)
            LogEvent.Level.INFO -> Log.i(fullTag, message, throwable)
            LogEvent.Level.WARN -> Log.w(fullTag, message, throwable)
            LogEvent.Level.ERROR -> Log.e(fullTag, message, throwable)
        }

        // Optionally write to file
        logDir?.let { dir ->
            try {
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val logFile = File(dir, "snap_pet_log.txt")
                val timestamp = dateFormat.format(Date(event.timestamp))
                val logLine = "$timestamp [${level.name}] $tag: $message\n"
                logFile.appendText(logLine)

                // Rotate log if too large (> 1MB)
                if (logFile.length() > 1024 * 1024) {
                    val backupFile = File(dir, "snap_pet_log_old.txt")
                    backupFile.delete()
                    logFile.renameTo(backupFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write to log file", e)
            }
        }
    }

    fun getRecentEvents(limit: Int = 100): List<LogEvent> {
        return events.toList().takeLast(limit)
    }

    fun clear() {
        events.clear()
    }
}
