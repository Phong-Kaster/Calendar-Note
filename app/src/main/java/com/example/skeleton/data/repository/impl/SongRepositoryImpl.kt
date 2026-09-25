package com.example.skeleton.data.repository.impl

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mediastore.SongRow
import com.example.skeleton.domain.model.Song
import com.example.skeleton.domain.repository.SongRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads the songs stored on the phone from Android's MediaStore (the phone's media index).
 *
 * It asks MediaStore for every audio file marked as music, sorted by title, turns every
 * row into a raw [SongRow], then lets the mapper clean it into a [Song].
 * If anything fails (for example the permission is missing) it logs the error and returns
 * an empty list, so the screen never crashes.
 *
 * Example:
 * ```kotlin
 * val repository: SongRepository = SongRepositoryImpl(context = appContext)
 * val songs = repository.getDeviceSongs()
 * ```
 *
 * @param context Used to reach the `ContentResolver`.
 * @param ioDispatcher Where the blocking MediaStore query runs; swap it in tests.
 * @author Phong-Kaster
 */
class SongRepositoryImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SongRepository {

    override suspend fun getDeviceSongs(): List<Song> = withContext(ioDispatcher) {
        try {
            querySongRows().mapNotNull { row -> row.toDomain() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getDeviceSongs() failed", e)
            emptyList()
        }
    }

    /**
     * Runs the MediaStore query and copies every row into a [SongRow].
     * Returns an empty list when MediaStore gives back no cursor.
     *
     * @author Phong-Kaster
     */
    private fun querySongRows(): List<SongRow> {
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            PROJECTION,
            SELECTION,
            null,
            SORT_ORDER,
        ) ?: return emptyList()

        return cursor.use { openedCursor -> readRows(cursor = openedCursor) }
    }

    /**
     * Walks the cursor from the first row to the last and builds one [SongRow] per row.
     *
     * @author Phong-Kaster
     */
    private fun readRows(cursor: Cursor): List<SongRow> {
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

        val rows = ArrayList<SongRow>(cursor.count)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            rows.add(
                SongRow(
                    id = id,
                    title = cursor.getString(titleColumn),
                    artist = cursor.getString(artistColumn),
                    album = cursor.getString(albumColumn),
                    durationMs = cursor.getLong(durationColumn),
                    displayName = cursor.getString(displayNameColumn),
                    contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id,
                    ).toString(),
                    albumId = readNullableLong(cursor = cursor, columnIndex = albumIdColumn),
                )
            )
        }
        return rows
    }

    /**
     * Reads a number from the cursor, or null when the cell is empty.
     * (A plain `getLong` would quietly turn an empty cell into 0.)
     *
     * @author Phong-Kaster
     */
    private fun readNullableLong(cursor: Cursor, columnIndex: Int): Long? {
        if (cursor.isNull(columnIndex)) return null
        return cursor.getLong(columnIndex)
    }

    companion object {
        private const val TAG = "SongRepositoryImpl"

        /** Only files MediaStore flags as music (skips ringtones, notifications, recordings). */
        private const val SELECTION = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        /** Sort songs by title, A to Z. */
        private const val SORT_ORDER = "${MediaStore.Audio.Media.TITLE} ASC"

        /** The columns we read from MediaStore. */
        private val PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM_ID,
        )
    }
}
