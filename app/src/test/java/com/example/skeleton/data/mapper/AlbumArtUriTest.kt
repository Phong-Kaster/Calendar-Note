package com.example.skeleton.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Checks that an album id becomes the right cover picture address, and that a missing
 * or impossible id gives no address.
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
}
