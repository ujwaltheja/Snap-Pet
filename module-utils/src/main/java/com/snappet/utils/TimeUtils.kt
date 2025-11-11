package com.snappet.utils

import kotlin.math.max

/**
 * Time utilities for calculating elapsed time and updating game state.
 */
object TimeUtils {
    /**
     * Calculate elapsed time in milliseconds between two timestamps.
     */
    fun elapsedMillis(from: Long, to: Long = System.currentTimeMillis()): Long {
        return max(0L, to - from)
    }

    /**
     * Calculate elapsed seconds between two timestamps.
     */
    fun elapsedSeconds(from: Long, to: Long = System.currentTimeMillis()): Float {
        return elapsedMillis(from, to) / 1000f
    }

    /**
     * Calculate elapsed minutes between two timestamps.
     */
    fun elapsedMinutes(from: Long, to: Long = System.currentTimeMillis()): Float {
        return elapsedSeconds(from, to) / 60f
    }
}
