package com.example.skeleton.injection

import com.example.skeleton.data.repository.impl.NoteRepositoryImpl
import com.example.skeleton.data.repository.impl.PostRepositoryImpl
import com.example.skeleton.data.repository.impl.SettingRepositoryImpl
import com.example.skeleton.data.repository.impl.UserActionRepositoryImpl
import com.example.skeleton.domain.repository.NoteRepository
import com.example.skeleton.domain.repository.PostRepository
import com.example.skeleton.domain.repository.SettingRepository
import com.example.skeleton.domain.repository.UserActionRepository
import org.koin.dsl.module

/**
 * Binds every repository interface to the implementation behind it.
 *
 * ViewModels ask for the **interface** — `NoteRepository`, never `NoteRepositoryImpl` — which is
 * what makes a ViewModel testable with a fake store.
 *
 * @author Phong-Kaster
 */
val repositoryModule = module {

    single<SettingRepository> { SettingRepositoryImpl(settingDatastore = get()) }

    // The next two are the skeleton's demo verticals, and since Home was repointed at notes
    // neither has a caller left: `UserActionRepository` lost its only one when `HomeViewModel`
    // stopped writing sample users, and `PostRepository` no longer feeds the Home list. They stay
    // wired on purpose — a working network vertical and a working Room vertical are part of what
    // makes this repository worth copying into the next project — not because anything still uses
    // them.
    single<UserActionRepository> { UserActionRepositoryImpl(dao = get()) }

    single<PostRepository> { PostRepositoryImpl(api = get(), dao = get()) }

    single<NoteRepository> { NoteRepositoryImpl(noteDao = get()) }
}