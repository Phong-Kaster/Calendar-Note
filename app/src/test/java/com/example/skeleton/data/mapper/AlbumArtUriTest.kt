package com.example.skeleton.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Checks that an album id becomes the right cover picture address, that a missing
 * or impossible id gives no address, and that the album id can be read back from an address.
 *
 * @author Phong-Kaster
 */
class AlbumArtUriTest {

    @Test
    fun validAlbumId_buildsAlbumArtUri() {
        assertEquals("content://media/external/audio/albumart/42", albumArtUriFor(albumId = 42L))
    }

    @Test
    fun nullAlbumId_returnsNull() {
        assertNull(albumArtUriFor(albumId = null))
    }

    @Test
    fun zeroAlbumId_returnsNull() {
        assertNull(albumArtUriFor(albumId = 0L))
    }

    @Test
    fun negativeAlbumId_returnsNull() {
        assertNull(albumArtUriFor(albumId = -1L))
    }

    @Test
    fun albumArtUri_parsesAlbumId() {
        assertEquals(42L, albumIdFromAlbumArtUri(uri = "content://media/external/audio/albumart/42"))
    }

    @Test
    fun nullUri_parsesToNull() {
        assertNull(albumIdFromAlbumArtUri(uri = null))
    }

    @Test
    fun zeroAlbumIdInUri_parsesToNull() {
        assertNull(albumIdFromAlbumArtUri(uri = "content://media/external/audio/albumart/0"))
    }

    @Test
    fun nonNumericAlbumIdInUri_parsesToNull() {
        assertNull(albumIdFromAlbumArtUri(uri = "content://media/external/audio/albumart/abc"))
    }

    @Test
    fun songUri_parsesToNull() {
        assertNull(albumIdFromAlbumArtUri(uri = "content://media/external/audio/media/42"))
    }

    @Test
    fun roundTrip_returnsSameAlbumId() {
        assertEquals(7L, albumIdFromAlbumArtUri(uri = albumArtUriFor(albumId = 7L)))
    }
}
