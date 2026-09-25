package com.example.skeleton.ui.fragment.music.model

/**
 * The one thing the Music screen shows in its body right now.
 *
 * - [Loading]: we are reading songs from the phone.
 * - [PermissionDenied]: the user has not allowed us to read audio files.
 * - [Empty]: allowed, but the phone has no songs.
 * - [Songs]: allowed, and we have a list to show.
 *
 * @author Phong-Kaster
 */
enum class MusicScreenContent {
    Loading,
    PermissionDenied,
    Empty,
    Songs,
}
