package com.example.simpleaudioplayer

import android.os.CountDownTimer
import android.util.Log
import java.util.concurrent.TimeUnit

class SleepTimerManager {
    
    private var countDownTimer: CountDownTimer? = null
    private var onTimerFinish: (() -> Unit)? = null
    private var onTimerTick: ((Long) -> Unit)? = null
    
    private var totalDuration = 0L
    private var elapsedTime = 0L
    private var isTimerRunning = false
    private var isTimerPaused = false
    private var isEnabled = true
    private var isAutoStop = true
    private var isFadeOut = false
    private var fadeOutDuration = 30L // seconds
    
    companion object {
        private const val TAG = "SleepTimerManager"
        private const val TICK_INTERVAL = 1000L // 1 second
    }
    
    fun startTimer(durationMillis: Long) {
        if (!isEnabled) return
        
        stopTimer()
        
        totalDuration = durationMillis
        elapsedTime = 0L
        isTimerRunning = true
        isTimerPaused = false
        
        countDownTimer = object : CountDownTimer(durationMillis, TICK_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime = totalDuration - millisUntilFinished
                onTimerTick?.invoke(millisUntilFinished)
                
                // Handle fade out
                if (isFadeOut && millisUntilFinished <= fadeOutDuration * 1000) {
                    handleFadeOut(millisUntilFinished)
                }
                
                Log.d(TAG, "Timer tick: ${formatTime(millisUntilFinished)} remaining")
            }
            
            override fun onFinish() {
                isTimerRunning = false
                isTimerPaused = false
                elapsedTime = totalDuration
                
                Log.d(TAG, "Timer finished")
                onTimerFinish?.invoke()
            }
        }.start()
        
        Log.d(TAG, "Timer started for ${formatTime(durationMillis)}")
    }
    
    fun pauseTimer() {
        if (isTimerRunning && !isTimerPaused) {
            countDownTimer?.cancel()
            isTimerPaused = true
            Log.d(TAG, "Timer paused")
        }
    }
    
    fun resumeTimer() {
        if (isTimerPaused) {
            val remainingTime = totalDuration - elapsedTime
            if (remainingTime > 0) {
                startTimer(remainingTime)
                Log.d(TAG, "Timer resumed")
            }
        }
    }
    
    fun cancelTimer() {
        stopTimer()
        Log.d(TAG, "Timer cancelled")
    }
    
    fun extendTimer(additionalMinutes: Int) {
        if (isTimerRunning) {
            val additionalMillis = TimeUnit.MINUTES.toMillis(additionalMinutes.toLong())
            val newTotalDuration = totalDuration + additionalMillis
            val remainingTime = totalDuration - elapsedTime + additionalMillis
            
            stopTimer()
            startTimer(remainingTime)
            
            Log.d(TAG, "Timer extended by $additionalMinutes minutes")
        }
    }
    
    fun setTimer(durationMillis: Long) {
        totalDuration = durationMillis
        Log.d(TAG, "Timer duration set to ${formatTime(durationMillis)}")
    }
    
    fun getRemainingTime(): Long {
        return if (isTimerRunning) {
            totalDuration - elapsedTime
        } else {
            0L
        }
    }
    
    fun getTotalDuration(): Long {
        return totalDuration
    }
    
    fun getElapsedTime(): Long {
        return elapsedTime
    }
    
    fun getCurrentState(): TimerState {
        return when {
            isTimerPaused -> TimerState.PAUSED
            isTimerRunning -> TimerState.RUNNING
            else -> TimerState.STOPPED
        }
    }
    
    fun isTimerRunning(): Boolean {
        return isTimerRunning
    }
    
    fun isTimerPaused(): Boolean {
        return isTimerPaused
    }
    
    fun isTimerActive(): Boolean {
        return isTimerRunning || isTimerPaused
    }
    
    fun getFormattedRemainingTime(): String {
        return formatTime(getRemainingTime())
    }
    
    fun getFormattedTotalTime(): String {
        return formatTime(totalDuration)
    }
    
    fun getFormattedElapsedTime(): String {
        return formatTime(elapsedTime)
    }
    
    fun getProgress(): Float {
        return if (totalDuration > 0) {
            elapsedTime.toFloat() / totalDuration
        } else {
            0f
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            stopTimer()
        }
        Log.d(TAG, "Timer ${if (enabled) "enabled" else "disabled"}")
    }
    
    fun setAutoStop(autoStop: Boolean) {
        isAutoStop = autoStop
        Log.d(TAG, "Auto stop ${if (autoStop) "enabled" else "disabled"}")
    }
    
    fun setFadeOut(fadeOut: Boolean) {
        isFadeOut = fadeOut
        Log.d(TAG, "Fade out ${if (fadeOut) "enabled" else "disabled"}")
    }
    
    fun setFadeOutDuration(durationSeconds: Long) {
        fadeOutDuration = durationSeconds
        Log.d(TAG, "Fade out duration set to ${durationSeconds}s")
    }
    
    fun isEnabled(): Boolean {
        return isEnabled
    }
    
    fun isAutoStopEnabled(): Boolean {
        return isAutoStop
    }
    
    fun isFadeOutEnabled(): Boolean {
        return isFadeOut
    }
    
    fun getFadeOutDuration(): Long {
        return fadeOutDuration
    }
    
    fun getPresetDurations(): List<Long> {
        return listOf(
            TimeUnit.MINUTES.toMillis(15),
            TimeUnit.MINUTES.toMillis(30),
            TimeUnit.MINUTES.toMillis(45),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(30),
            TimeUnit.HOURS.toMillis(2)
        )
    }
    
    fun getTimerInfo(): TimerInfo {
        return TimerInfo(
            state = getCurrentState(),
            remainingTime = getRemainingTime(),
            totalDuration = totalDuration,
            elapsedTime = elapsedTime,
            progress = getProgress(),
            isEnabled = isEnabled,
            isAutoStop = isAutoStop,
            isFadeOut = isFadeOut,
            fadeOutDuration = fadeOutDuration
        )
    }
    
    fun getTimerSettings(): TimerSettings {
        return TimerSettings(
            isEnabled = isEnabled,
            isAutoStop = isAutoStop,
            isFadeOut = isFadeOut,
            fadeOutDuration = fadeOutDuration
        )
    }
    
    fun applySettings(settings: TimerSettings) {
        isEnabled = settings.isEnabled
        isAutoStop = settings.isAutoStop
        isFadeOut = settings.isFadeOut
        fadeOutDuration = settings.fadeOutDuration
        
        Log.d(TAG, "Timer settings applied")
    }
    
    fun setOnTimerFinish(callback: () -> Unit) {
        onTimerFinish = callback
    }
    
    fun setOnTimerTick(callback: (Long) -> Unit) {
        onTimerTick = callback
    }
    
    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        isTimerRunning = false
        isTimerPaused = false
    }
    
    private fun handleFadeOut(millisUntilFinished: Long) {
        val fadeOutProgress = millisUntilFinished / (fadeOutDuration * 1000f)
        // This would typically control volume fade out
        // Implementation depends on the audio player
        Log.d(TAG, "Fade out progress: ${(1f - fadeOutProgress) * 100}%")
    }
    
    private fun formatTime(milliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%d:%02d", minutes, seconds)
            else -> String.format("%ds", seconds)
        }
    }
    
    fun release() {
        stopTimer()
        onTimerFinish = null
        onTimerTick = null
        Log.d(TAG, "Sleep timer manager released")
    }
    
    enum class TimerState {
        RUNNING, PAUSED, STOPPED
    }
    
    data class TimerInfo(
        val state: TimerState,
        val remainingTime: Long,
        val totalDuration: Long,
        val elapsedTime: Long,
        val progress: Float,
        val isEnabled: Boolean,
        val isAutoStop: Boolean,
        val isFadeOut: Boolean,
        val fadeOutDuration: Long
    )
    
    data class TimerSettings(
        val isEnabled: Boolean,
        val isAutoStop: Boolean,
        val isFadeOut: Boolean,
        val fadeOutDuration: Long
    )
}

// Extension functions for convenience
fun Long.toMinutes(): Long = this / 60000
fun Long.toSeconds(): Long = this / 1000
fun Long.toHours(): Long = this / 3600000

fun Int.toMillis(): Long = this * 1000L
fun Long.toMillis(): Long = this * 1000L
