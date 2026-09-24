package com.example.skeleton.domain.repository

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Song

/**
 * Gives access to the songs that are already stored on the device.
 *
 * Example:
 * ```kotlin
 * when (val outcome = songRepository.getSongs()) {
 *     is Outcome.Success -> showSongs(outcome.data)
 *     is Outcome.Error -> showEmpty()
 *     is Outcome.Loading -> Unit
 * }
 * ```
 * @author Phong-Kaster
 */
interface SongRepository {

    /**
     * Reads every music file on the device, sorted by title.
     * Never throws: returns [Outcome.Success] with the list (maybe empty) or [Outcome.Error].
     * @author Phong-Kaster
     */
    suspend fun getSongs(): Outcome<List<Song>>
}
