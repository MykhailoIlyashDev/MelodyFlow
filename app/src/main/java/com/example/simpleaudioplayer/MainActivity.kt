package com.example.simpleaudioplayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    // Core Components
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private lateinit var playlistAdapter: PlaylistAdapter
    private val audioTracks = mutableListOf<AudioTrack>()
    private var currentTrackIndex = -1
    private var isPlaying = false
    private var isRepeatTrack = false
    private var isRepeatPlaylist = false
    private var isShuffle = false
    private var currentVolume = 1.0f
    private var currentPosition = 0L
    private var currentDuration = 0L
    
    // UI Components
    private lateinit var btnPlayPause: MaterialButton
    private lateinit var btnPrevious: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var btnRepeatTrack: MaterialButton
    private lateinit var btnRepeatPlaylist: MaterialButton
    private lateinit var btnShuffle: MaterialButton
    private lateinit var btnAddAudio: MaterialButton
    private lateinit var btnSettings: MaterialButton
    private lateinit var sliderProgress: Slider
    private lateinit var sliderVolume: Slider
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvVolume: TextView
    private lateinit var tvNowPlaying: TextView
    private lateinit var recyclerView: RecyclerView
    
    // Permissions
    private val PERMISSION_REQUEST_CODE = 123
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    
    private val pickAudioLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris != null) {
            addAudioFiles(uris)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupMediaPlayer()
        setupRecyclerView()
        setupClickListeners()
        setupSliders()
        
        // Request permissions
        if (!checkPermissions()) {
            requestPermissions()
        }
    }
    
    private fun initializeViews() {
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnRepeatTrack = findViewById(R.id.btnRepeatTrack)
        btnRepeatPlaylist = findViewById(R.id.btnRepeatPlaylist)
        btnShuffle = findViewById(R.id.btnShuffle)
        btnAddAudio = findViewById(R.id.btnAddAudio)
        btnSettings = findViewById(R.id.btnSettings)
        sliderProgress = findViewById(R.id.sliderProgress)
        sliderVolume = findViewById(R.id.sliderVolume)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        tvVolume = findViewById(R.id.tvVolume)
        tvNowPlaying = findViewById(R.id.tvNowPlaying)
        recyclerView = findViewById(R.id.recyclerView)
        
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            if (isRepeatTrack) {
                playCurrentTrack()
            } else {
                playNextTrack()
            }
        }
        
        mediaPlayer.setOnPreparedListener {
            currentDuration = mediaPlayer.duration.toLong()
            updateProgress()
            startProgressUpdates()
        }
        
        mediaPlayer.setOnErrorListener { _, what, extra ->
            showToast("Playback error: $what")
            false
        }
        
        // Set initial volume
        mediaPlayer.setVolume(currentVolume, currentVolume)
        sliderVolume.value = currentVolume * 100
        updateVolumeDisplay()
    }
    
    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(audioTracks) { position ->
            playTrack(position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = playlistAdapter
    }
    
    private fun setupClickListeners() {
        btnPlayPause.setOnClickListener {
            if (audioTracks.isEmpty()) {
                showToast("Додайте аудіо файли спочатку")
                return@setOnClickListener
            }
            
            if (currentTrackIndex == -1) {
                playTrack(0)
            } else {
                togglePlayPause()
            }
        }
        
        btnPrevious.setOnClickListener {
            playPreviousTrack()
        }
        
        btnNext.setOnClickListener {
            playNextTrack()
        }
        
        btnRepeatTrack.setOnClickListener {
            toggleRepeatTrack()
        }
        
        btnRepeatPlaylist.setOnClickListener {
            toggleRepeatPlaylist()
        }
        
        btnShuffle.setOnClickListener {
            toggleShuffle()
        }
        
        btnAddAudio.setOnClickListener {
            if (checkPermissions()) {
                pickAudioFiles()
            }
        }
        
        btnSettings.setOnClickListener {
            showSettingsDialog()
        }
    }
    
    private fun setupSliders() {
        sliderProgress.addOnChangeListener { _, value, fromUser ->
            if (fromUser && mediaPlayer.isPlaying) {
                val newPosition = (value * currentDuration).toLong()
                mediaPlayer.seekTo(newPosition.toInt())
                currentPosition = newPosition
                updateProgress()
            }
        }
        
        sliderVolume.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                currentVolume = value / 100f
                mediaPlayer.setVolume(currentVolume, currentVolume)
                updateVolumeDisplay()
            }
        }
        
        // Set initial values
        sliderVolume.value = currentVolume * 100
    }
    
    private fun checkPermissions(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
    
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showToast("Всі дозволи надано")
            } else {
                showToast("Потрібні дозволи для доступу до аудіо файлів")
            }
        }
    }
    
    private fun pickAudioFiles() {
        pickAudioLauncher.launch("audio/*")
    }
    
    private fun addAudioFiles(uris: List<Uri>) {
        val newTracks = mutableListOf<AudioTrack>()
        var errorCount = 0
        
        for (uri in uris) {
            try {
                val track = createAudioTrackFromUri(uri)
                if (track != null && !audioTracks.contains(track)) {
                    newTracks.add(track)
                } else if (track == null) {
                    errorCount++
                    showToast("Не вдалося створити трек для: ${uri.lastPathSegment ?: "невідомий файл"}")
                }
            } catch (e: Exception) {
                errorCount++
                showToast("Помилка додавання файлу: ${e.message}")
            }
        }
        
        if (newTracks.isNotEmpty()) {
            audioTracks.addAll(newTracks)
            playlistAdapter.notifyDataSetChanged()
            updateTrackCount()
            
            val message = if (errorCount > 0) {
                "Додано ${newTracks.size} файлів, помилок: $errorCount"
            } else {
                "Додано ${newTracks.size} аудіо файлів"
            }
            showToast(message)
            
            if (currentTrackIndex == -1) {
                currentTrackIndex = 0
                updateNowPlaying()
            }
        } else if (errorCount > 0) {
            showToast("Не вдалося додати жодного файлу. Перевірте дозволи та формат файлів.")
        }
    }
    
    private fun createAudioTrackFromUri(uri: Uri): AudioTrack? {
        Log.d(TAG, "Creating audio track from URI: $uri")
        return try {
            // Спробуємо отримати метадані через MediaStore
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use { cursorInner ->
                if (cursorInner.moveToFirst()) {
                    val nameIndex = cursorInner.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    val durationIndex = cursorInner.getColumnIndex(MediaStore.MediaColumns.DURATION)
                    val sizeIndex = cursorInner.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    
                    val name = if (nameIndex >= 0) cursorInner.getString(nameIndex) else null
                    val duration = if (durationIndex >= 0) cursorInner.getLong(durationIndex) else 0L
                    val size = if (sizeIndex >= 0) cursorInner.getLong(sizeIndex) else 0L
                    
                    if (name != null) {
                        AudioTrack(
                            id = System.currentTimeMillis() + Random().nextLong(),
                            title = name,
                            artist = "Unknown Artist",
                            album = "Unknown Album",
                            duration = duration,
                            path = uri.toString(),
                            size = size,
                            lastModified = System.currentTimeMillis()
                        )
                    } else {
                        // Якщо не можемо отримати назву, використовуємо URI
                        val fileName = uri.lastPathSegment ?: "Unknown Track"
                        AudioTrack(
                            id = System.currentTimeMillis() + Random().nextLong(),
                            title = fileName,
                            artist = "Unknown Artist",
                            album = "Unknown Album",
                            duration = 0L,
                            path = uri.toString(),
                            size = 0L,
                            lastModified = System.currentTimeMillis()
                        )
                    }
                } else {
                    // Якщо cursor порожній, створюємо базовий трек
                    val fileName = uri.lastPathSegment ?: "Unknown Track"
                    AudioTrack(
                        id = System.currentTimeMillis() + Random().nextLong(),
                        title = fileName,
                        artist = "Unknown Artist",
                        album = "Unknown Album",
                        duration = 0L,
                        path = uri.toString(),
                        size = 0L,
                        lastModified = System.currentTimeMillis()
                    )
                }
            } ?: run {
                // Якщо cursor == null, створюємо базовий трек
                val fileName = uri.lastPathSegment ?: "Unknown Track"
                AudioTrack(
                    id = System.currentTimeMillis() + Random().nextLong(),
                    title = fileName,
                    artist = "Unknown Artist",
                    album = "Unknown Album",
                    duration = 0L,
                    path = uri.toString(),
                    size = 0L,
                    lastModified = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            // Якщо виникла помилка, створюємо базовий трек
            Log.e(TAG, "Error creating audio track from URI: $uri", e)
            val fileName = uri.lastPathSegment ?: "Unknown Track"
            AudioTrack(
                id = System.currentTimeMillis() + Random().nextLong(),
                title = fileName,
                artist = "Unknown Artist",
                album = "Unknown Album",
                duration = 0L,
                path = uri.toString(),
                size = 0L,
                lastModified = System.currentTimeMillis()
            )
        }
    }
    
    private fun playTrack(position: Int) {
        if (position < 0 || position >= audioTracks.size) return
        
        try {
            currentTrackIndex = position
            val track = audioTracks[position]
            
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, Uri.parse(track.path))
            mediaPlayer.prepareAsync()
            
            isPlaying = true
            updatePlayPauseButton()
            updateNowPlaying()
            
            showToast("Грає: ${track.title}")
            
        } catch (e: Exception) {
            showToast("Помилка відтворення: ${e.message}")
        }
    }
    
    private fun playCurrentTrack() {
        if (currentTrackIndex >= 0 && currentTrackIndex < audioTracks.size) {
            playTrack(currentTrackIndex)
        }
    }
    
    private fun togglePlayPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
            currentPosition = mediaPlayer.currentPosition.toLong()
        } else {
            mediaPlayer.start()
            isPlaying = true
            startProgressUpdates()
        }
        updatePlayPauseButton()
    }
    
    private fun playPreviousTrack() {
        if (audioTracks.isEmpty()) return
        
        val newIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else audioTracks.size - 1
        playTrack(newIndex)
    }
    
    private fun playNextTrack() {
        if (audioTracks.isEmpty()) return
        
        val newIndex = if (currentTrackIndex < audioTracks.size - 1) currentTrackIndex + 1 else 0
        playTrack(newIndex)
    }
    
    private fun toggleRepeatTrack() {
        isRepeatTrack = !isRepeatTrack
        isRepeatPlaylist = false
        updateRepeatButtons()
        showToast(if (isRepeatTrack) "Повтор треку увімкнено" else "Повтор треку вимкнено")
    }
    
    private fun toggleRepeatPlaylist() {
        isRepeatPlaylist = !isRepeatPlaylist
        isRepeatTrack = false
        updateRepeatButtons()
        showToast(if (isRepeatPlaylist) "Повтор плейлиста увімкнено" else "Повтор плейлиста вимкнено")
    }
    
    private fun toggleShuffle() {
        isShuffle = !isShuffle
        updateShuffleButton()
        showToast(if (isShuffle) "Перемішування увімкнено" else "Перемішування вимкнено")
    }
    
    private fun updatePlayPauseButton() {
        btnPlayPause.text = if (isPlaying) "⏸️" else "▶️"
    }
    
    private fun updateRepeatButtons() {
        btnRepeatTrack.text = if (isRepeatTrack) "🔂" else "🔂"
        btnRepeatPlaylist.text = if (isRepeatPlaylist) "🔁" else "🔁"
    }
    
    private fun updateShuffleButton() {
        btnShuffle.text = if (isShuffle) "🔀" else "🔀"
    }
    
    private fun updateNowPlaying() {
        if (currentTrackIndex >= 0 && currentTrackIndex < audioTracks.size) {
            val track = audioTracks[currentTrackIndex]
            tvNowPlaying.text = "${track.title} - ${track.artist}"
        } else {
            tvNowPlaying.text = "Нічого не грає"
        }
    }
    
    private fun updateTrackCount() {
        // Track count is shown in the playlist
    }
    
    private fun updateVolumeDisplay() {
        val volumePercent = (currentVolume * 100).toInt()
        tvVolume.text = "$volumePercent%"
    }
    
    private fun updateProgress() {
        if (currentDuration > 0) {
            val progress = currentPosition.toFloat() / currentDuration
            sliderProgress.value = progress
            tvCurrentTime.text = formatTime(currentPosition.toInt())
            tvTotalTime.text = formatTime(currentDuration.toInt())
        }
    }
    
    private fun startProgressUpdates() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    currentPosition = mediaPlayer.currentPosition.toLong()
                    updateProgress()
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }
    
    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
    
    private fun showSettingsDialog() {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Налаштування")
            .setMessage("Версія додатку: 1.0.0\n\nЦе простий аудіо плеєр з базовими функціями.")
            .setPositiveButton("OK", null)
            .create()
        dialog.show()
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}
