package com.example.simpleaudioplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import android.view.View
import android.app.Activity
import com.google.android.material.slider.Slider
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddAudio: Button
    private lateinit var btnPlayPause: Button
    private lateinit var btnNext: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnRepeatTrack: Button
    private lateinit var btnRepeatPlaylist: Button
    private lateinit var sliderProgress: Slider
    private lateinit var sliderVolume: Slider
    private lateinit var tvCurrentTrack: TextView
    private lateinit var tvTrackCount: TextView
    
    private lateinit var playlistAdapter: PlaylistAdapter
    private val audioTracks = mutableListOf<AudioTrack>()
    private var currentTrackIndex = 0
    private var isPlaying = false
    private var isRepeatTrack = false
    private var isRepeatPlaylist = false
    
    private val pickAudioLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            addAudioFiles(uris)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupClickListeners()
        checkPermission()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewPlaylist)
        btnAddAudio = findViewById(R.id.btnAddAudio)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnRepeatTrack = findViewById(R.id.btnRepeatTrack)
        btnRepeatPlaylist = findViewById(R.id.btnRepeatPlaylist)
        sliderProgress = findViewById(R.id.sliderProgress)
        sliderVolume = findViewById(R.id.sliderVolume)
        tvCurrentTrack = findViewById(R.id.tvCurrentTrack)
        tvTrackCount = findViewById(R.id.tvPlaylistCount)
        
        playlistAdapter = PlaylistAdapter(audioTracks) { position ->
            playTrack(position)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = playlistAdapter
    }
    
    private fun setupClickListeners() {
        btnAddAudio.setOnClickListener {
            if (checkPermission()) {
                pickAudioFiles()
            }
        }
        
        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }
        
        btnNext.setOnClickListener {
            nextTrack()
        }
        
        btnPrevious.setOnClickListener {
            previousTrack()
        }
        
        btnRepeatTrack.setOnClickListener {
            toggleRepeatTrack()
        }
        
        btnRepeatPlaylist.setOnClickListener {
            toggleRepeatPlaylist()
        }
        
        sliderProgress.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                showMessage("Seeking to: ${formatTime(value.toLong())}")
            }
        }
        
        sliderVolume.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                showMessage("Volume: ${value.toInt()}%")
            }
        }
    }
    
    private fun checkPermission(): Boolean {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - використовуємо READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            
            // Android 13+ - запитуємо дозвіл на повідомлення
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 12 та нижче - використовуємо READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray())
            return false
        }
        return true
    }
    
    private fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(
            this,
            permissions,
            PERMISSION_REQUEST_CODE
        )
    }
    
    private fun pickAudioFiles() {
        pickAudioLauncher.launch("audio/*")
    }
    
    private fun addAudioFiles(uris: List<Uri>) {
        for (uri in uris) {
            val trackName = getFileName(uri)
            val audioTrack = AudioTrack(uri, trackName, 0L)
            if (!audioTracks.contains(audioTrack)) {
                audioTracks.add(audioTrack)
            }
        }
        updatePlaylist()
        updateTrackCount()
        showMessage("Added ${uris.size} audio files")
    }
    
    private fun getFileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex) ?: "Unknown Track"
        } ?: "Unknown Track"
    }
    
    private fun updatePlaylist() {
        playlistAdapter.notifyDataSetChanged()
    }
    
    private fun updateTrackCount() {
        tvTrackCount.text = "Tracks: ${audioTracks.size}"
    }
    
    private fun playTrack(position: Int) {
        if (position in audioTracks.indices) {
            currentTrackIndex = position
            val track = audioTracks[position]
            tvCurrentTrack.text = "Now Playing: ${track.name}"
            isPlaying = true
            updatePlayPauseButton()
            showMessage("Playing: ${track.name}")
        }
    }
    
    private fun togglePlayPause() {
        if (audioTracks.isNotEmpty()) {
            isPlaying = !isPlaying
            updatePlayPauseButton()
            val action = if (isPlaying) "Playing" else "Paused"
            showMessage(action)
        } else {
            showMessage("No tracks in playlist")
        }
    }
    
    private fun updatePlayPauseButton() {
        btnPlayPause.text = if (isPlaying) "⏸️" else "▶️"
    }
    
    private fun nextTrack() {
        if (audioTracks.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % audioTracks.size
            playTrack(currentTrackIndex)
        }
    }
    
    private fun previousTrack() {
        if (audioTracks.isNotEmpty()) {
            currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else audioTracks.size - 1
            playTrack(currentTrackIndex)
        }
    }
    
    private fun toggleRepeatTrack() {
        isRepeatTrack = !isRepeatTrack
        btnRepeatTrack.text = if (isRepeatTrack) "Repeat Track: ON" else "Repeat Track: OFF"
        showMessage("Repeat Track: ${if (isRepeatTrack) "ON" else "OFF"}")
    }
    
    private fun toggleRepeatPlaylist() {
        isRepeatPlaylist = !isRepeatPlaylist
        btnRepeatPlaylist.text = if (isRepeatPlaylist) "Repeat Playlist: ON" else "Repeat Playlist: OFF"
        showMessage("Repeat Playlist: ${if (isRepeatPlaylist) "ON" else "OFF"}")
    }
    
    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
    
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()
            
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }
            
            if (deniedPermissions.isEmpty()) {
                showMessage("All permissions granted")
            } else {
                showPermissionDeniedDialog(deniedPermissions)
            }
        }
    }
    
    private fun showPermissionDeniedDialog(deniedPermissions: List<String>) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permissions Required")
            .setMessage("This app needs audio access permissions to play music files. Please grant the required permissions in Settings.")
            .setPositiveButton("Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
