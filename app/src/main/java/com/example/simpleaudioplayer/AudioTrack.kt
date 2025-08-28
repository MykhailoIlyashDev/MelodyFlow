package com.example.simpleaudioplayer

data class AudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val size: Long,
    val lastModified: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioTrack
        return path == other.path
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}
