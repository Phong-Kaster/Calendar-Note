package com.example.skeleton.data.mapper

import com.example.skeleton.data.mediastore.SongRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Checks the rules that turn a raw MediaStore [SongRow] into a clean song.
 *
 * @author Phong-Kaster
 */
class SongMapperTest {

    /** Builds a valid row; each test changes only the field it cares about. */
    private fun row(
        title: String? = "Yesterday",
        artist: String? = "The Beatles",
        album: String? = "Help!",
        durationMs: Long = 125_000L,
        displayName: String? = "yesterday.mp3",
    ): SongRow = SongRow(
        id = 42L,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        displayName = displayName,
        contentUri = "content://media/external/audio/media/42",
    )

    @Test
    fun validRow_mapsEveryField() {
        val song = row().toDomain()

        assertNotNull(song)
        assertEquals(42L, song!!.id)
        assertEquals("Yesterday", song.title)
        assertEquals("The Beatles", song.artist)
        assertEquals("Help!", song.album)
        assertEquals(125_000L, song.durationMs)
        assertEquals("content://media/external/audio/media/42", song.contentUri)
    }

    @Test
    fun zeroDuration_returnsNull() {
        assertNull(row(durationMs = 0L).toDomain())
    }

    @Test
    fun negativeDuration_returnsNull() {
        assertNull(row(durationMs = -1L).toDomain())
    }

    @Test
    fun blankTitle_usesDisplayNameWithoutExtension() {
        assertEquals("my_song", row(title = "   ", displayName = "my_song.mp3").toDomain()!!.title)
    }

    @Test
    fun nullTitle_usesDisplayNameWithoutExtension() {
        assertEquals("track.01", row(title = null, displayName = "track.01.flac").toDomain()!!.title)
    }

    @Test
    fun blankTitle_displayNameWithoutDot_usesWholeDisplayName() {
        assertEquals("recording", row(title = "", displayName = "recording").toDomain()!!.title)
    }

    @Test
    fun blankTitleAndBlankDisplayName_returnsNull() {
        assertNull(row(title = " ", displayName = "  ").toDomain())
    }

    @Test
    fun nullTitleAndNullDisplayName_returnsNull() {
        assertNull(row(title = null, displayName = null).toDomain())
    }

    @Test
    fun blankTitle_displayNameOnlyExtension_returnsNull() {
        assertNull(row(title = "", displayName = ".mp3").toDomain())
    }

    @Test
    fun unknownArtist_mapsToNull() {
        assertNull(row(artist = "<unknown>").toDomain()!!.artist)
    }

    @Test
    fun blankArtist_mapsToNull() {
        assertNull(row(artist = "  ").toDomain()!!.artist)
    }

    @Test
    fun nullArtist_mapsToNull() {
        assertNull(row(artist = null).toDomain()!!.artist)
    }
}
