package com.turkcell.lyraapp.data.player

import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val playerState: StateFlow<GlobalPlayerState>

    fun playSong(songId: String)
    fun playNext()
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun release()
    
    fun downloadCurrentSong()
    fun removeDownload()
}
