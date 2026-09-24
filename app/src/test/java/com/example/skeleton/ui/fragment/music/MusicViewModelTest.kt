package com.example.skeleton.ui.fragment.music

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.Song
import com.example.skeleton.domain.repository.PlayerRepository
import com.example.skeleton.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Checks the Music screen states driven by [MusicViewModel.onPermissionResult] and the
 * playback actions forwarded to [PlayerRepository].
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

    /** One call the ViewModel made on the player. */
    private data class PlayQueueCall(val songs: List<Song>, val startIndex: Int)

    /**
     * Hand-written fake player: records every command; [emit] pushes a new state.
     * Models the connect/release pairing like the real one: [activeClients] goes up on
     * connect(), down on release(), never below 0.
     */
    private class FakePlayerRepository : PlayerRepository {
        private val _playbackState = MutableStateFlow(PlaybackState.Idle)
        override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

        var connectCalls = 0
        var releaseCalls = 0
        var activeClients = 0
        var togglePlayPauseCalls = 0
        var nextCalls = 0
        var previousCalls = 0
        val playQueueCalls = mutableListOf<PlayQueueCall>()

        fun emit(state: PlaybackState) {
            _playbackState.value = state
        }

        override fun connect() {
            connectCalls++
            activeClients++
        }

        override fun release() {
            releaseCalls++
            if (activeClients > 0) activeClients--
        }

        override fun playQueue(songs: List<Song>, startIndex: Int) {
            playQueueCalls.add(PlayQueueCall(songs = songs, startIndex = startIndex))
        }

        override fun togglePlayPause() {
            togglePlayPauseCalls++
        }

        override fun next() {
            nextCalls++
        }

        override fun previous() {
            previousCalls++
        }
    }

    private fun song(id: Long, title: String) = Song(
        id = id,
        title = title,
        artist = "Artist",
        durationMs = 1_000L,
        contentUri = "content://media/external/audio/media/$id",
    )

    private fun viewModelWith(
        songRepository: SongRepository,
        playerRepository: PlayerRepository = FakePlayerRepository(),
    ) = MusicViewModel(songRepository = songRepository, playerRepository = playerRepository)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- Permission and song list ----------

    @Test
    fun initialState_isLoadingUntilPermissionIsKnown() {
        val viewModel = viewModelWith(songRepository = FakeSongRepository(Outcome.Success(emptyList())))
        assertEquals(MusicScreenContent.Loading, viewModel.uiState.value.content)
    }

    @Test
    fun denied_showsPermissionNeeded_andDoesNotLoad() = runTest {
        val repository = FakeSongRepository(Outcome.Success(listOf(song(1, "A"))))
        val viewModel = viewModelWith(songRepository = repository)

        viewModel.onPermissionResult(granted = false)

        val state = viewModel.uiState.value
        assertEquals(MusicPermissionState.Denied, state.permissionState)
        assertEquals(MusicScreenContent.PermissionNeeded, state.content)
        assertTrue(state.songs.isEmpty())
        assertEquals(0, repository.calls)
    }

    @Test
    fun grantedAndNoSongs_showsEmpty() = runTest {
        val viewModel = viewModelWith(songRepository = FakeSongRepository(Outcome.Success(emptyList())))

        viewModel.onPermissionResult(granted = true)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(MusicScreenContent.Empty, state.content)
    }

    @Test
    fun grantedWithSongs_showsListInRepositoryOrder() = runTest {
        val songs = listOf(song(3, "Alpha"), song(1, "Beta"), song(2, "Gamma"))
        val viewModel = viewModelWith(songRepository = FakeSongRepository(Outcome.Success(songs)))

        viewModel.onPermissionResult(granted = true)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(MusicScreenContent.SongList, state.content)
        assertEquals(songs, state.songs)
    }

    @Test
    fun repositoryError_notLoading_andEmpty() = runTest {
        val viewModel = viewModelWith(songRepository = FakeSongRepository(Outcome.Error(message = "boom")))

        viewModel.onPermissionResult(granted = true)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.songs.isEmpty())
        assertEquals(MusicScreenContent.Empty, state.content)
    }

    @Test
    fun grantedThenRevoked_showsPermissionNeeded_andClearsList() = runTest {
        val viewModel = viewModelWith(songRepository = FakeSongRepository(Outcome.Success(listOf(song(1, "A")))))

        viewModel.onPermissionResult(granted = true)
        viewModel.onPermissionResult(granted = false)

        val state = viewModel.uiState.value
        assertEquals(MusicScreenContent.PermissionNeeded, state.content)
        assertTrue(state.songs.isEmpty())
    }

    // ---------- Playback ----------

    @Test
    fun init_connectsToPlayer_once() {
        val player = FakePlayerRepository()

        viewModelWith(songRepository = FakeSongRepository(Outcome.Success(emptyList())), playerRepository = player)

        assertEquals(1, player.connectCalls)
        assertEquals(0, player.releaseCalls)
        assertEquals(1, player.activeClients)
    }

    @Test
    fun twoViewModels_shareOnePlayer_eachConnectsOnce_andBothMirrorState() {
        // Music -> Home -> Music makes a second ViewModel while the first one still exists.
        val player = FakePlayerRepository()
        val songRepository = FakeSongRepository(Outcome.Success(emptyList()))

        val oldViewModel = viewModelWith(songRepository = songRepository, playerRepository = player)
        val newViewModel = viewModelWith(songRepository = songRepository, playerRepository = player)

        assertEquals(2, player.connectCalls)
        assertEquals(0, player.releaseCalls)
        assertEquals(2, player.activeClients)

        val playing = PlaybackState(songId = 7, title = "Hey Jude", artist = "The Beatles", isPlaying = true, hasQueue = true)
        player.emit(state = playing)
        assertEquals(playing, oldViewModel.uiState.value.playback)
        assertEquals(playing, newViewModel.uiState.value.playback)

        // A pause from the notification must still reach the newer screen.
        player.emit(state = playing.copy(isPlaying = false))
        assertFalse(newViewModel.uiState.value.playback.isPlaying)
        assertTrue(newViewModel.uiState.value.showNowPlayingBar)
    }

    @Test
    fun fakePlayer_releaseNeverDropsBelowZero() {
        // Guards the fake itself so the counting assertions above stay meaningful.
        val player = FakePlayerRepository()

        player.connect()
        player.release()
        player.release()

        assertEquals(0, player.activeClients)
        assertEquals(2, player.releaseCalls)
    }

    @Test
    fun songClick_playsWholeListFromTappedIndex() = runTest {
        val songs = listOf(song(3, "Alpha"), song(1, "Beta"), song(2, "Gamma"))
        val player = FakePlayerRepository()
        val viewModel = viewModelWith(songRepository = FakeSongRepository(Outcome.Success(songs)), playerRepository = player)
        viewModel.onPermissionResult(granted = true)

        viewModel.onSongClick(song = songs[2])
        viewModel.onSongClick(song = songs[0])

        assertEquals(
            listOf(
                PlayQueueCall(songs = songs, startIndex = 2),
                PlayQueueCall(songs = songs, startIndex = 0),
            ),
            player.playQueueCalls,
        )
    }

    @Test
    fun songClick_forSongNotInList_doesNothing() = runTest {
        val player = FakePlayerRepository()
        val viewModel = viewModelWith(
            songRepository = FakeSongRepository(Outcome.Success(listOf(song(1, "A")))),
            playerRepository = player,
        )
        viewModel.onPermissionResult(granted = true)

        viewModel.onSongClick(song = song(99, "Not in list"))

        assertTrue(player.playQueueCalls.isEmpty())
    }

    @Test
    fun playPauseNextPrevious_eachReachThePlayer() {
        val player = FakePlayerRepository()
        val viewModel = viewModelWith(songRepository = FakeSongRepository(Outcome.Success(emptyList())), playerRepository = player)

        viewModel.onTogglePlayPause()
        viewModel.onNext()
        viewModel.onNext()
        viewModel.onPrevious()

        assertEquals(1, player.togglePlayPauseCalls)
        assertEquals(2, player.nextCalls)
        assertEquals(1, player.previousCalls)
        assertTrue(player.playQueueCalls.isEmpty())
    }

    @Test
    fun beforeAnythingPlays_nowPlayingBarIsHidden() {
        val viewModel = viewModelWith(songRepository = FakeSongRepository(Outcome.Success(emptyList())))

        val state = viewModel.uiState.value
        assertEquals(PlaybackState.Idle, state.playback)
        assertFalse(state.showNowPlayingBar)
        assertNull(state.playback.title)
    }

    @Test
    fun uiState_mirrorsPlayerState() {
        val player = FakePlayerRepository()
        val viewModel = viewModelWith(songRepository = FakeSongRepository(Outcome.Success(emptyList())), playerRepository = player)

        val playingYesterday = PlaybackState(songId = 1, title = "Yesterday", artist = "The Beatles", isPlaying = true, hasQueue = true)
        player.emit(state = playingYesterday)
        assertEquals(playingYesterday, viewModel.uiState.value.playback)
        assertTrue(viewModel.uiState.value.showNowPlayingBar)

        player.emit(state = playingYesterday.copy(isPlaying = false))
        assertFalse(viewModel.uiState.value.playback.isPlaying)
        assertTrue(viewModel.uiState.value.showNowPlayingBar)

        player.emit(state = PlaybackState(songId = 2, title = "Imagine", artist = null, isPlaying = true, hasQueue = true))
        assertEquals("Imagine", viewModel.uiState.value.playback.title)
        assertEquals(2L, viewModel.uiState.value.playback.songId)
        assertNull(viewModel.uiState.value.playback.artist)
        assertTrue(viewModel.uiState.value.playback.isPlaying)
    }

    @Test
    fun playerState_survivesSongListReload() = runTest {
        val player = FakePlayerRepository()
        val viewModel = viewModelWith(
            songRepository = FakeSongRepository(Outcome.Success(listOf(song(1, "A")))),
            playerRepository = player,
        )
        val playing = PlaybackState(songId = 1, title = "A", artist = "Artist", isPlaying = true, hasQueue = true)
        player.emit(state = playing)

        viewModel.onPermissionResult(granted = true)

        assertEquals(playing, viewModel.uiState.value.playback)
    }
}
