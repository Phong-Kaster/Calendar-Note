package com.example.skeleton.injection



import com.example.skeleton.ui.fragment.home.HomeViewModel
import com.example.skeleton.ui.fragment.setting.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    // Setting View Model
    viewModel { SettingViewModel(settingRepository = get()) }

    // Home View Model
    viewModel { HomeViewModel(userActionRepository = get(), postRepository = get()) }
}