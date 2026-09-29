package com.example.skeleton.injection



import com.example.skeleton.ui.fragment.library.LibraryViewModel
import com.example.skeleton.ui.fragment.nowplaying.NowPlayingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    // Library View Model
    viewModel { LibraryViewModel(musicRepository = get(), playerRepository = get()) }

    // Now Playing View Model
    viewModel { NowPlayingViewModel(playerRepository = get()) }
}