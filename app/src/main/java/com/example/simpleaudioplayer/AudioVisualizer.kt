package com.example.simpleaudioplayer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class AudioVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private var visualizer: Visualizer? = null
    private var audioSessionId: Int = 0
    
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val path = Path()
    
    private var waveformData: ByteArray? = null
    private var fftData: ByteArray? = null
    
    private var visualizationType = VisualizationType.WAVEFORM
    private var barWidth = 4f
    private var barSpacing = 2f
    private var cornerRadius = 8f
    
    private var primaryColor = Color.parseColor("#FF6200EE")
    private var secondaryColor = Color.parseColor("#FF03DAC5")
    private var backgroundColor = Color.parseColor("#FF121212")
    
    private var isVisualizing = false
    
    enum class VisualizationType {
        WAVEFORM,
        BARS,
        CIRCLE,
        SPECTRUM
    }
    
    fun setAudioSessionId(sessionId: Int) {
        if (audioSessionId != sessionId) {
            audioSessionId = sessionId
            releaseVisualizer()
            initializeVisualizer()
        }
    }
    
    private fun initializeVisualizer() {
        if (audioSessionId == 0) return
        
        try {
            visualizer = Visualizer(audioSessionId).apply {
                enabled = false
                captureSize = Visualizer.getCaptureSizeRange()[1]
                
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (visualizationType == VisualizationType.WAVEFORM) {
                                waveformData = waveform
                                postInvalidate()
                            }
                        }
                        
                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (visualizationType != VisualizationType.WAVEFORM) {
                                fftData = fft
                                postInvalidate()
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    false
                )
                
                enabled = true
                isVisualizing = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setVisualizationType(type: VisualizationType) {
        visualizationType = type
        invalidate()
    }
    
    fun setColors(primary: Int, secondary: Int, background: Int) {
        primaryColor = primary
        secondaryColor = secondary
        backgroundColor = background
        invalidate()
    }
    
    fun setBarDimensions(width: Float, spacing: Float, radius: Float) {
        barWidth = width
        barSpacing = spacing
        cornerRadius = radius
        invalidate()
    }
    
    fun startVisualization() {
        visualizer?.enabled = true
        isVisualizing = true
    }
    
    fun stopVisualization() {
        visualizer?.enabled = false
        isVisualizing = false
        waveformData = null
        fftData = null
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (!isVisualizing) {
            drawPlaceholder(canvas)
            return
        }
        
        when (visualizationType) {
            VisualizationType.WAVEFORM -> drawWaveform(canvas)
            VisualizationType.BARS -> drawBars(canvas)
            VisualizationType.CIRCLE -> drawCircle(canvas)
            VisualizationType.SPECTRUM -> drawSpectrum(canvas)
        }
    }
    
    private fun drawPlaceholder(canvas: Canvas) {
        paint.color = backgroundColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        paint.color = primaryColor
        paint.textSize = 24f
        paint.textAlign = Paint.Align.CENTER
        
        val text = "No Audio"
        val x = width / 2f
        val y = height / 2f + paint.textSize / 2
        
        canvas.drawText(text, x, y, paint)
    }
    
    private fun drawWaveform(canvas: Canvas) {
        val data = waveformData ?: return
        
        paint.color = backgroundColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        paint.color = primaryColor
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        
        val centerY = height / 2f
        val scaleY = height / 2f / 128f
        
        path.reset()
        path.moveTo(0f, centerY)
        
        for (i in data.indices) {
            val x = (i.toFloat() / data.size) * width
            val y = centerY + (data[i].toFloat() * scaleY)
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawBars(canvas: Canvas) {
        val data = fftData ?: return
        
        paint.color = backgroundColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        val barCount = (width / (barWidth + barSpacing)).toInt()
        val dataStep = max(1, data.size / barCount)
        
        for (i in 0 until barCount) {
            val dataIndex = i * dataStep
            if (dataIndex < data.size) {
                val magnitude = sqrt(
                    data[dataIndex].toDouble().pow(2) + 
                    data[dataIndex + 1].toDouble().pow(2)
                ).toFloat()
                
                val barHeight = (magnitude / 128f) * height
                val x = i * (barWidth + barSpacing)
                val y = height - barHeight
                
                paint.color = interpolateColor(primaryColor, secondaryColor, magnitude / 128f)
                paint.style = Paint.Style.FILL
                
                canvas.drawRoundRect(
                    x, y, x + barWidth, height.toFloat(),
                    cornerRadius, cornerRadius, paint
                )
            }
        }
    }
    
    private fun drawCircle(canvas: Canvas) {
        val data = fftData ?: return
        
        paint.color = backgroundColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) / 3f
        
        val barCount = 64
        val dataStep = max(1, data.size / barCount)
        
        for (i in 0 until barCount) {
            val dataIndex = i * dataStep
            if (dataIndex < data.size) {
                val magnitude = sqrt(
                    data[dataIndex].toDouble().pow(2) + 
                    data[dataIndex + 1].toDouble().pow(2)
                ).toFloat()
                
                val angle = (i.toFloat() / barCount) * 2 * PI.toFloat()
                val barLength = (magnitude / 128f) * radius
                
                val startX = centerX + cos(angle) * radius
                val startY = centerY + sin(angle) * radius
                val endX = centerX + cos(angle) * (radius + barLength)
                val endY = centerY + sin(angle) * (radius + barLength)
                
                paint.color = interpolateColor(primaryColor, secondaryColor, magnitude / 128f)
                paint.strokeWidth = 4f
                paint.style = Paint.Style.STROKE
                
                canvas.drawLine(startX, startY, endX, endY, paint)
            }
        }
    }
    
    private fun drawSpectrum(canvas: Canvas) {
        val data = fftData ?: return
        
        paint.color = backgroundColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        val barCount = (width / (barWidth + barSpacing)).toInt()
        val dataStep = max(1, data.size / barCount)
        
        for (i in 0 until barCount) {
            val dataIndex = i * dataStep
            if (dataIndex < data.size) {
                val magnitude = sqrt(
                    data[dataIndex].toDouble().pow(2) + 
                    data[dataIndex + 1].toDouble().pow(2)
                ).toFloat()
                
                val barHeight = (magnitude / 128f) * height
                val x = i * (barWidth + barSpacing)
                val y = height - barHeight
                
                // Create gradient effect
                val gradient = android.graphics.LinearGradient(
                    x, y, x, height.toFloat(),
                    primaryColor, secondaryColor,
                    android.graphics.Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                paint.style = Paint.Style.FILL
                
                canvas.drawRoundRect(
                    x, y, x + barWidth, height.toFloat(),
                    cornerRadius, cornerRadius, paint
                )
                
                paint.shader = null
            }
        }
    }
    
    private fun interpolateColor(color1: Int, color2: Int, ratio: Float): Int {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        
        val r = (r1 + (r2 - r1) * ratio).toInt()
        val g = (g1 + (g2 - g1) * ratio).toInt()
        val b = (b1 + (b2 - b1) * ratio).toInt()
        
        return Color.rgb(r, g, b)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releaseVisualizer()
    }
    
    private fun releaseVisualizer() {
        visualizer?.let { viz ->
            viz.enabled = false
            viz.release()
        }
        visualizer = null
        isVisualizing = false
    }
    
    fun isVisualizing(): Boolean = isVisualizing
    
    fun getVisualizationType(): VisualizationType = visualizationType
    
    fun getColors(): Triple<Int, Int, Int> = Triple(primaryColor, secondaryColor, backgroundColor)
}
