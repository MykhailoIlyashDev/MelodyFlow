package com.example.simpleaudioplayer

import android.net.Uri

data class AudioTrack(
    val uri: Uri,
    val name: String,
    val duration: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioTrack
        return uri == other.uri
    }

    override fun hashCode(): Int {
        return uri.hashCode()
    }
}
