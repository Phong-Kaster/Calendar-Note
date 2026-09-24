package com.example.skeleton.domain.model

/**
 * One song that already lives on the device (read from the system music library).
 *
 * Example: `Song(id = 42, title = "Yesterday", artist = "The Beatles", durationMs = 125_000, contentUri = "content://media/external/audio/media/42")`
 *
 * @param id Unique id of the song inside the device music library.
 * @param title Song name. Can be an empty string when the file has no title; the UI then shows a fallback text.
 * @param artist Artist name, or null when unknown (the UI then shows a fallback text).
 * @param durationMs How long the song plays, in milliseconds.
 * @param contentUri Text form of the address a player uses to open the song.
 * @author Phong-Kaster
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val durationMs: Long,
    val contentUri: String,
)
