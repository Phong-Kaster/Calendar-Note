package com.example.skeleton

import com.example.skeleton.data.mapper.MediaStoreSongRow
import com.example.skeleton.data.mapper.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Checks that a raw MediaStore row becomes the right [com.example.skeleton.domain.model.Song].
 *
 * @author Phong-Kaster
 */
class SongMapperTest {

    @Test
    fun toDomain_copiesFieldsAndBuildsUris() {
        val row = MediaStoreSongRow(
            id = 42L,
            title = "Blue Sky",
            artist = "The Band",
            album = "Summer",
            albumId = 7L,
            durationMs = 215_000L,
        )

        val song = row.toDomain()

        assertEquals(42L, song.id)
        assertEquals("Blue Sky", song.title)
        assertEquals("The Band", song.artist)
        assertEquals("Summer", song.album)
        assertEquals(215_000L, song.durationMs)
        assertEquals("content://media/external/audio/media/42", song.contentUri)
        assertEquals("content://media/external/audio/albumart/7", song.artworkUri)
    }

    @Test
    fun toDomain_nullTextBecomesEmpty_unknownArtistKeptRaw() {
        val row = MediaStoreSongRow(
            id = 1L,
            title = null,
            artist = "<unknown>",
            album = null,
            albumId = 3L,
            durationMs = 1_000L,
        )

        val song = row.toDomain()

        assertEquals("", song.title)
        assertEquals("<unknown>", song.artist)
        assertEquals("", song.album)
    }

    @Test
    fun toDomain_nullArtistBecomesEmpty() {
        val row = MediaStoreSongRow(id = 1L, title = "T", artist = null, album = "A", albumId = 3L, durationMs = 1L)

        assertEquals("", row.toDomain().artist)
    }

    @Test
    fun toDomain_noAlbumId_hasNoArtwork() {
        val row = MediaStoreSongRow(id = 1L, title = "T", artist = "A", album = "B", albumId = 0L, durationMs = 1L)

        assertNull(row.toDomain().artworkUri)
    }

    @Test
    fun toDomain_negativeDuration_becomesZero() {
        val row = MediaStoreSongRow(id = 1L, title = "T", artist = "A", album = "B", albumId = 1L, durationMs = -5L)

        assertEquals(0L, row.toDomain().durationMs)
    }
}
