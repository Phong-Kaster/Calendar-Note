package com.example.skeleton.ui.fragment.music

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Song
import com.example.skeleton.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Checks the Music screen states driven by [MusicViewModel.onPermissionResult].
 * @author Phong-Kaster
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModelTest {

    /** Hand-written fake: returns [outcome] and counts calls. */
    private class FakeSongRepository(private val outcome: Outcome<List<Song>>) : SongRepository {
        var calls = 0
        override suspend fun getSongs(): Outcome<List<Song>> {
            calls++
            return outcome
        }
    }

    private fun song(id: Long, title: String) = Song(
        id = id,
        title = title,
        artist = "Artist",
        durationMs = 1_000L,
        contentUri = "content://media/external/audio/media/$id",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoadingUntilPermissionIsKnown() {
        val viewModel = MusicViewModel(songRepository = FakeSongRepository(Outcome.Success(emptyList())))
        assertEquals(MusicScreenContent.Loading, viewModel.uiState.value.content)
    }

    @Test
    fun denied_showsPermissionNeeded_andDoesNotLoad() = runTest {
        val repository = FakeSongRepository(Outcome.Success(listOf(song(1, "A"))))
        val viewModel = MusicViewModel(songRepository = repository)

        viewModel.onPermissionResult(granted = false)

        val state = viewModel.uiState.value
        assertEquals(MusicPermissionState.Denied, state.permissionState)
        assertEquals(MusicScreenContent.PermissionNeeded, state.content)
        assertTrue(state.songs.isEmpty())
        assertEquals(0, repository.calls)
    }

    @Test
    fun grantedAndNoSongs_showsEmpty() = runTest {
        val viewModel = MusicViewModel(songRepository = FakeSongRepository(Outcome.Success(emptyList())))

        viewModel.onPermissionResult(granted = true)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(MusicScreenContent.Empty, state.content)
    }

    @Test
    fun grantedWithSongs_showsListInRepositoryOrder() = runTest {
        val songs = listOf(song(3, "Alpha"), song(1, "Beta"), song(2, "Gamma"))
        val viewModel = MusicViewModel(songRepository = FakeSongRepository(Outcome.Success(songs)))

        viewModel.onPermissionResult(granted = true)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(MusicScreenContent.SongList, state.content)
        assertEquals(songs, state.songs)
    }

    @Test
    fun repositoryError_notLoading_andEmpty() = runTest {
        val viewModel = MusicViewModel(songRepository = FakeSongRepository(Outcome.Error(message = "boom")))

        viewModel.onPermissionResult(granted = true)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.songs.isEmpty())
        assertEquals(MusicScreenContent.Empty, state.content)
    }

    @Test
    fun grantedThenRevoked_showsPermissionNeeded_andClearsList() = runTest {
        val viewModel = MusicViewModel(songRepository = FakeSongRepository(Outcome.Success(listOf(song(1, "A")))))

        viewModel.onPermissionResult(granted = true)
        viewModel.onPermissionResult(granted = false)

        val state = viewModel.uiState.value
        assertEquals(MusicScreenContent.PermissionNeeded, state.content)
        assertTrue(state.songs.isEmpty())
    }
}
