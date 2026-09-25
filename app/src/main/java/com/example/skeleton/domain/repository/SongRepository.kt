package com.example.skeleton.domain.repository

import com.example.skeleton.domain.model.Song

/**
 * Gives the app the list of songs stored on the phone.
 *
 * Example:
 * ```kotlin
 * val songs: List<Song> = songRepository.getDeviceSongs()
 * ```
 *
 * @author Phong-Kaster
 */
interface SongRepository {

    /**
     * Reads every music file on the phone, sorted by title (A to Z).
     * Never throws: returns an empty list when something goes wrong or no permission is granted.
     */
    suspend fun getDeviceSongs(): List<Song>
}
