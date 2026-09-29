package com.example.skeleton.domain.repository

import com.example.skeleton.domain.model.Song

/**
 * Gives the app the list of songs stored on the phone.
 *
 * Example:
 * ```kotlin
 * val songs: List<Song> = musicRepository.getSongs()
 * ```
 *
 * @author Phong-Kaster
 */
interface MusicRepository {

    /**
     * Reads every music file on the phone, sorted by title.
     * Never throws: returns an empty list when nothing can be read (e.g. no permission).
     */
    suspend fun getSongs(): List<Song>
}
