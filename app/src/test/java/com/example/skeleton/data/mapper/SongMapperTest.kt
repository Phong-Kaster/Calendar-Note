package com.example.skeleton.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks the music-library query and the row → [com.example.skeleton.domain.model.Song] rules.
 * @author Phong-Kaster
 */
class SongMapperTest {

    private val base = "content://media/external/audio/media"

    private fun row(
        id: Long = 7L,
        title: String? = "Song",
        artist: String? = "Artist",
        durationMs: Long? = 185_000L,
    ) = SongRow(id = id, title = title, artist = artist, durationMs = durationMs)

    // ---------- Query ----------

    @Test
    fun selection_keepsOnlyMusic() {
        assertEquals("is_music != 0", SongQuery.selection)
    }

    @Test
    fun sortOrder_isByTitleAscending() {
        assertEquals("title ASC", SongQuery.sortOrder)
    }

    @Test
    fun projection_hasNeededColumns() {
        val columns = SongQuery.projection.toList()
        assertTrue(columns.contains("_id"))
        assertTrue(columns.contains("title"))
        assertTrue(columns.contains("artist"))
        assertTrue(columns.contains("duration"))
    }

    // ---------- Row mapping ----------

    @Test
    fun toDomain_mapsAllFields() {
        val song = row().toDomain(contentUriBase = base)
        assertEquals(7L, song.id)
        assertEquals("Song", song.title)
        assertEquals("Artist", song.artist)
        assertEquals(185_000L, song.durationMs)
        assertEquals("content://media/external/audio/media/7", song.contentUri)
    }

    @Test
    fun toDomain_trailingSlashInBase_doesNotDoubleSlash() {
        val song = row(id = 3L).toDomain(contentUriBase = "$base/")
        assertEquals("content://media/external/audio/media/3", song.contentUri)
    }

    @Test
    fun toDomain_unknownArtistPlaceholder_becomesNull() {
        assertNull(row(artist = "<unknown>").toDomain(contentUriBase = base).artist)
    }

    @Test
    fun toDomain_blankArtist_becomesNull() {
        assertNull(row(artist = "   ").toDomain(contentUriBase = base).artist)
    }

    @Test
    fun toDomain_nullArtist_staysNull() {
        assertNull(row(artist = null).toDomain(contentUriBase = base).artist)
    }

    @Test
    fun toDomain_nullOrBlankTitle_becomesEmptyForUiFallback() {
        assertEquals("", row(title = null).toDomain(contentUriBase = base).title)
        assertEquals("", row(title = "  ").toDomain(contentUriBase = base).title)
    }

    @Test
    fun toDomain_titleIsTrimmed() {
        assertEquals("Song", row(title = "  Song ").toDomain(contentUriBase = base).title)
    }

    @Test
    fun toDomain_nullOrNegativeDuration_becomesZero() {
        assertEquals(0L, row(durationMs = null).toDomain(contentUriBase = base).durationMs)
        assertEquals(0L, row(durationMs = -5L).toDomain(contentUriBase = base).durationMs)
    }

    // ---------- formatDuration ----------

    @Test
    fun formatDuration_minutesAndPaddedSeconds() {
        assertEquals("3:05", formatDuration(durationMs = 185_000L))
    }

    @Test
    fun formatDuration_zero() {
        assertEquals("0:00", formatDuration(durationMs = 0L))
    }

    @Test
    fun formatDuration_negative_isZero() {
        assertEquals("0:00", formatDuration(durationMs = -1_000L))
    }

    @Test
    fun formatDuration_dropsMilliseconds() {
        assertEquals("0:59", formatDuration(durationMs = 59_999L))
    }

    @Test
    fun formatDuration_overAnHour_keepsCountingMinutes() {
        assertEquals("60:00", formatDuration(durationMs = 3_600_000L))
    }
}
