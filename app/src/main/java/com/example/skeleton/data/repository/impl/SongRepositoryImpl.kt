package com.example.skeleton.data.repository.impl

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.skeleton.common.Outcome
import com.example.skeleton.data.mapper.SongQuery
import com.example.skeleton.data.mapper.SongRow
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.domain.model.Song
import com.example.skeleton.domain.repository.SongRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads the device's music from `MediaStore.Audio.Media` (the system music library).
 * The query itself (columns, filter, order) and the row cleaning live in `SongMapper.kt`.
 *
 * Example: `SongRepositoryImpl(context = androidContext()).getSongs()`
 * @param context Used to reach the ContentResolver.
 * @param ioDispatcher Where the blocking query runs.
 * @author Phong-Kaster
 */
class SongRepositoryImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SongRepository {

    /**
     * Queries every music file, sorted by title. Returns [Outcome.Error] instead of throwing
     * (for example when the permission is missing).
     * @author Phong-Kaster
     */
    override suspend fun getSongs(): Outcome<List<Song>> = withContext(ioDispatcher) {
        try {
            Outcome.Success(querySongs())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getSongs() failed", e)
            Outcome.Error(message = e.message ?: "Unknown error", throwable = e)
        }
    }

    /**
     * Walks the cursor row by row and turns each row into a [Song]. Blocking — call on [ioDispatcher].
     * @author Phong-Kaster
     */
    private fun querySongs(): List<Song> {
        val collectionUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val contentUriBase = collectionUri.toString()
        val cursor = context.contentResolver.query(
            collectionUri,
            SongQuery.projection,
            SongQuery.selection,
            null,
            SongQuery.sortOrder,
        ) ?: return emptyList()

        return cursor.use { rows ->
            val idIndex = rows.getColumnIndexOrThrow(SongQuery.COLUMN_ID)
            val titleIndex = rows.getColumnIndexOrThrow(SongQuery.COLUMN_TITLE)
            val artistIndex = rows.getColumnIndexOrThrow(SongQuery.COLUMN_ARTIST)
            val durationIndex = rows.getColumnIndexOrThrow(SongQuery.COLUMN_DURATION)

            val songs = ArrayList<Song>(rows.count)
            while (rows.moveToNext()) {
                val row = SongRow(
                    id = rows.getLong(idIndex),
                    title = if (rows.isNull(titleIndex)) null else rows.getString(titleIndex),
                    artist = if (rows.isNull(artistIndex)) null else rows.getString(artistIndex),
                    durationMs = if (rows.isNull(durationIndex)) null else rows.getLong(durationIndex),
                )
                songs.add(row.toDomain(contentUriBase = contentUriBase))
            }
            songs
        }
    }

    companion object {
        private const val TAG = "SongRepositoryImpl"
    }
}
