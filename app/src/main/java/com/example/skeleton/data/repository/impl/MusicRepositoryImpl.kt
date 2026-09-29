package com.example.skeleton.data.repository.impl

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.skeleton.data.mapper.MediaStoreSongRow
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.domain.model.Song
import com.example.skeleton.domain.repository.MusicRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads songs from the phone's MediaStore (the system list of media files).
 * Only files marked as music (`IS_MUSIC != 0`) are returned, sorted by title.
 * If anything goes wrong (for example the permission is missing) it returns an empty list
 * instead of crashing.
 *
 * Example:
 * ```kotlin
 * val repository: MusicRepository = MusicRepositoryImpl(context = androidContext())
 * val songs = repository.getSongs()
 * ```
 *
 * @param context used to reach the `ContentResolver`.
 * @param ioDispatcher where the blocking query runs; tests may swap it.
 * @author Phong-Kaster
 */
class MusicRepositoryImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MusicRepository {

    override suspend fun getSongs(): List<Song> = withContext(ioDispatcher) {
        try {
            querySongRows().map { row -> row.toDomain() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getSongs() failed", e)
            emptyList()
        }
    }

    /**
     * Asks MediaStore for every music file and copies each row into a [MediaStoreSongRow].
     * Blocking — call only on [ioDispatcher].
     *
     * @author Phong-Kaster
     */
    private fun querySongRows(): List<MediaStoreSongRow> {
        val rows = mutableListOf<MediaStoreSongRow>()
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            PROJECTION,
            SELECTION,
            null,
            SORT_ORDER,
        ) ?: return rows

        cursor.use { songCursor ->
            val idColumn = songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (songCursor.moveToNext()) {
                rows.add(
                    MediaStoreSongRow(
                        id = songCursor.getLong(idColumn),
                        title = songCursor.getString(titleColumn),
                        artist = songCursor.getString(artistColumn),
                        album = songCursor.getString(albumColumn),
                        albumId = songCursor.getLong(albumIdColumn),
                        durationMs = songCursor.getLong(durationColumn),
                    )
                )
            }
        }
        return rows
    }

    companion object {
        private const val TAG = "MusicRepositoryImpl"

        /** Columns we read for each song. */
        private val PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
        )

        /** Only real music (skips ringtones, notifications, voice notes). */
        private const val SELECTION = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        /** Alphabetical by title, ignoring upper/lower case. */
        private const val SORT_ORDER = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
    }
}
