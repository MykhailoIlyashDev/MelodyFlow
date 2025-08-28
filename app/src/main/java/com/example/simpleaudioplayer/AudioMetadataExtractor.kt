package com.example.simpleaudioplayer

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.util.*

class AudioMetadataExtractor(private val context: Context) {
    
    private val mediaMetadataRetriever = MediaMetadataRetriever()
    
    companion object {
        private const val TAG = "AudioMetadataExtractor"
        
        // Common metadata keys
        const val KEY_TITLE = MediaMetadataRetriever.METADATA_KEY_TITLE
        const val KEY_ARTIST = MediaMetadataRetriever.METADATA_KEY_ARTIST
        const val KEY_ALBUM = MediaMetadataRetriever.METADATA_KEY_ALBUM
        const val KEY_GENRE = MediaMetadataRetriever.METADATA_KEY_GENRE
        const val KEY_YEAR = MediaMetadataRetriever.METADATA_KEY_YEAR
        const val KEY_DURATION = MediaMetadataRetriever.METADATA_KEY_DURATION
        const val KEY_TRACK_NUMBER = MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER
        const val KEY_DISC_NUMBER = MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER
        const val KEY_COMPOSER = MediaMetadataRetriever.METADATA_KEY_COMPOSER
        const val KEY_WRITER = MediaMetadataRetriever.METADATA_KEY_WRITER
        const val KEY_DATE = MediaMetadataRetriever.METADATA_KEY_DATE
        const val KEY_BITRATE = MediaMetadataRetriever.METADATA_KEY_BITRATE
        const val KEY_SAMPLERATE = MediaMetadataRetriever.METADATA_KEY_SAMPLERATE
        const val KEY_CHANNELS = MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS
    }
    
    data class AudioMetadata(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val genre: String? = null,
        val year: Int? = null,
        val duration: Long? = null,
        val trackNumber: Int? = null,
        val discNumber: Int? = null,
        val composer: String? = null,
        val writer: String? = null,
        val date: String? = null,
        val bitrate: Int? = null,
        val sampleRate: Int? = null,
        val channels: Int? = null,
        val fileSize: Long? = null,
        val filePath: String? = null,
        val mimeType: String? = null,
        val lastModified: Long? = null
    )
    
    fun extractMetadata(uri: Uri): AudioMetadata? {
        return try {
            mediaMetadataRetriever.setDataSource(context, uri)
            extractMetadataFromRetriever(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract metadata from URI: ${e.message}")
            null
        }
    }
    
    fun extractMetadata(filePath: String): AudioMetadata? {
        return try {
            mediaMetadataRetriever.setDataSource(filePath)
            extractMetadataFromRetriever(Uri.fromFile(File(filePath)))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract metadata from file: ${e.message}")
            null
        }
    }
    
    private fun extractMetadataFromRetriever(uri: Uri): AudioMetadata {
        val file = getFileFromUri(uri)
        
        return AudioMetadata(
            title = getStringMetadata(KEY_TITLE),
            artist = getStringMetadata(KEY_ARTIST),
            album = getStringMetadata(KEY_ALBUM),
            genre = getStringMetadata(KEY_GENRE),
            year = getIntMetadata(KEY_YEAR),
            duration = getLongMetadata(KEY_DURATION),
            trackNumber = getIntMetadata(KEY_TRACK_NUMBER),
            discNumber = getIntMetadata(KEY_DISC_NUMBER),
            composer = getStringMetadata(KEY_COMPOSER),
            writer = getStringMetadata(KEY_WRITER),
            date = getStringMetadata(KEY_DATE),
            bitrate = getIntMetadata(KEY_BITRATE),
            sampleRate = getIntMetadata(KEY_SAMPLERATE),
            channels = getIntMetadata(KEY_CHANNELS),
            fileSize = file?.length(),
            filePath = file?.absolutePath,
            mimeType = getMimeType(uri),
            lastModified = file?.lastModified()
        )
    }
    
    private fun getStringMetadata(key: Int): String? {
        return try {
            val value = mediaMetadataRetriever.extractMetadata(key)
            if (value.isNullOrBlank()) null else value.trim()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract string metadata for key $key: ${e.message}")
            null
        }
    }
    
    private fun getIntMetadata(key: Int): Int? {
        return try {
            val value = mediaMetadataRetriever.extractMetadata(key)
            value?.toIntOrNull()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract int metadata for key $key: ${e.message}")
            null
        }
    }
    
    private fun getLongMetadata(key: Int): Long? {
        return try {
            val value = mediaMetadataRetriever.extractMetadata(key)
            value?.toLongOrNull()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract long metadata for key $key: ${e.message}")
            null
        }
    }
    
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            when (uri.scheme) {
                "file" -> File(uri.path ?: "")
                "content" -> {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = File.createTempFile("audio_", ".tmp", context.cacheDir)
                    
                    inputStream?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    tempFile
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get file from URI: ${e.message}")
            null
        }
    }
    
    private fun getMimeType(uri: Uri): String? {
        return try {
            context.contentResolver.getType(uri)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get MIME type: ${e.message}")
            null
        }
    }
    
    // Advanced metadata extraction
    fun extractDetailedMetadata(uri: Uri): DetailedAudioMetadata? {
        val basicMetadata = extractMetadata(uri) ?: return null
        
        return try {
            val file = getFileFromUri(uri)
            val detailedMetadata = DetailedAudioMetadata(
                basic = basicMetadata,
                fileInfo = extractFileInfo(file),
                audioInfo = extractAudioInfo(uri),
                id3Tags = extractID3Tags(uri),
                artwork = extractArtwork(uri)
            )
            
            detailedMetadata
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract detailed metadata: ${e.message}")
            null
        }
    }
    
    private fun extractFileInfo(file: File?): FileInfo? {
        if (file == null || !file.exists()) return null
        
        return try {
            FileInfo(
                name = file.name,
                absolutePath = file.absolutePath,
                size = file.length(),
                lastModified = file.lastModified(),
                isReadable = file.canRead(),
                isWritable = file.canWrite(),
                extension = getFileExtension(file.name),
                parentDirectory = file.parent
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract file info: ${e.message}")
            null
        }
    }
    
    private fun extractAudioInfo(uri: Uri): AudioInfo? {
        return try {
            val duration = getLongMetadata(KEY_DURATION)
            val bitrate = getIntMetadata(KEY_BITRATE)
            val sampleRate = getIntMetadata(KEY_SAMPLERATE)
            val channels = getIntMetadata(KEY_CHANNELS)
            
            if (duration != null || bitrate != null || sampleRate != null || channels != null) {
                AudioInfo(
                    duration = duration,
                    bitrate = bitrate,
                    sampleRate = sampleRate,
                    channels = channels,
                    format = getAudioFormat(uri),
                    quality = calculateAudioQuality(bitrate, sampleRate, channels)
                )
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract audio info: ${e.message}")
            null
        }
    }
    
    private fun extractID3Tags(uri: Uri): ID3Tags? {
        // This would require a dedicated ID3 tag library
        // For now, return basic info from MediaMetadataRetriever
        return try {
            ID3Tags(
                title = getStringMetadata(KEY_TITLE),
                artist = getStringMetadata(KEY_ARTIST),
                album = getStringMetadata(KEY_ALBUM),
                genre = getStringMetadata(KEY_GENRE),
                year = getIntMetadata(KEY_YEAR),
                trackNumber = getIntMetadata(KEY_TRACK_NUMBER),
                discNumber = getIntMetadata(KEY_DISC_NUMBER),
                composer = getStringMetadata(KEY_COMPOSER),
                writer = getStringMetadata(KEY_WRITER),
                date = getStringMetadata(KEY_DATE),
                comment = null, // Not available in MediaMetadataRetriever
                lyrics = null,  // Not available in MediaMetadataRetriever
                bpm = null,     // Not available in MediaMetadataRetriever
                key = null      // Not available in MediaMetadataRetriever
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract ID3 tags: ${e.message}")
            null
        }
    }
    
    private fun extractArtwork(uri: Uri): ArtworkInfo? {
        return try {
            val artwork = mediaMetadataRetriever.embeddedPicture
            if (artwork != null) {
                ArtworkInfo(
                    data = artwork,
                    size = artwork.size,
                    mimeType = getArtworkMimeType(artwork),
                    dimensions = getArtworkDimensions(artwork)
                )
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract artwork: ${e.message}")
            null
        }
    }
    
    private fun getFileExtension(fileName: String): String? {
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex > 0) {
            fileName.substring(lastDotIndex + 1).lowercase()
        } else null
    }
    
    private fun getAudioFormat(uri: Uri): String? {
        val mimeType = getMimeType(uri)
        return when {
            mimeType?.contains("mp3") == true -> "MP3"
            mimeType?.contains("wav") == true -> "WAV"
            mimeType?.contains("m4a") == true -> "M4A"
            mimeType?.contains("aac") == true -> "AAC"
            mimeType?.contains("ogg") == true -> "OGG"
            mimeType?.contains("flac") == true -> "FLAC"
            else -> mimeType?.substringAfterLast('/')?.uppercase()
        }
    }
    
    private fun calculateAudioQuality(bitrate: Int?, sampleRate: Int?, channels: Int?): String {
        val quality = when {
            bitrate != null && bitrate >= 320000 -> "High"
            bitrate != null && bitrate >= 192000 -> "Good"
            bitrate != null && bitrate >= 128000 -> "Standard"
            else -> "Unknown"
        }
        
        val sampleRateInfo = when {
            sampleRate != null && sampleRate >= 48000 -> "48kHz+"
            sampleRate != null && sampleRate >= 44100 -> "44.1kHz"
            else -> ""
        }
        
        val channelInfo = when (channels) {
            1 -> "Mono"
            2 -> "Stereo"
            else -> ""
        }
        
        return listOf(quality, sampleRateInfo, channelInfo).filter { it.isNotEmpty() }.joinToString(" ")
    }
    
    private fun getArtworkMimeType(artworkData: ByteArray): String? {
        return when {
            artworkData.size >= 2 && artworkData[0] == 0xFF.toByte() && artworkData[1] == 0xD8.toByte() -> "image/jpeg"
            artworkData.size >= 8 && artworkData.take(8).toByteArray().contentEquals("PNG\r\n\u001A\n".toByteArray()) -> "image/png"
            else -> "image/unknown"
        }
    }
    
    private fun getArtworkDimensions(artworkData: ByteArray): Pair<Int, Int>? {
        // This would require image decoding
        // For now, return null
        return null
    }
    
    // Utility functions
    fun getSupportedFormats(): List<String> {
        return listOf("MP3", "WAV", "M4A", "AAC", "OGG", "FLAC")
    }
    
    fun isFormatSupported(uri: Uri): Boolean {
        val mimeType = getMimeType(uri)
        return mimeType?.contains("audio/") == true
    }
    
    fun cleanup() {
        try {
            mediaMetadataRetriever.release()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup metadata retriever: ${e.message}")
        }
    }
    
    // Data classes for detailed metadata
    data class DetailedAudioMetadata(
        val basic: AudioMetadata,
        val fileInfo: FileInfo?,
        val audioInfo: AudioInfo?,
        val id3Tags: ID3Tags?,
        val artwork: ArtworkInfo?
    )
    
    data class FileInfo(
        val name: String,
        val absolutePath: String,
        val size: Long,
        val lastModified: Long,
        val isReadable: Boolean,
        val isWritable: Boolean,
        val extension: String?,
        val parentDirectory: String?
    )
    
    data class AudioInfo(
        val duration: Long?,
        val bitrate: Int?,
        val sampleRate: Int?,
        val channels: Int?,
        val format: String?,
        val quality: String
    )
    
    data class ID3Tags(
        val title: String?,
        val artist: String?,
        val album: String?,
        val genre: String?,
        val year: Int?,
        val trackNumber: Int?,
        val discNumber: Int?,
        val composer: String?,
        val writer: String?,
        val date: String?,
        val comment: String?,
        val lyrics: String?,
        val bpm: Int?,
        val key: String?
    )
    
    data class ArtworkInfo(
        val data: ByteArray,
        val size: Int,
        val mimeType: String?,
        val dimensions: Pair<Int, Int>?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as ArtworkInfo
            return data.contentEquals(other.data) && size == other.size
        }
        
        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + size
            return result
        }
    }
}

