package com.example.skeleton.ui.fragment.home

import com.example.skeleton.domain.model.Note

/**
 * Everything the Home screen needs to draw itself.
 *
 * @param isLoading true until the store has answered for the first time. It starts **true** on
 *   purpose: reading from the database is asynchronous, so a state that starts "not loading, no
 *   notes" would flash the "no notes yet" message at somebody who has fifty of them. Once the
 *   first list arrives it is false forever — later updates arrive as new lists, not as reloads.
 * @param notes every note in the app, most recently touched first. The store guarantees the order;
 *   this screen does not re-sort it.
 * @author Phong-Kaster
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
)
