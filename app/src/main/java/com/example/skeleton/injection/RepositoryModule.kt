package com.example.skeleton.injection

import com.example.skeleton.data.repository.impl.MusicRepositoryImpl
import com.example.skeleton.domain.repository.MusicRepository
import com.example.skeleton.domain.repository.PlayerRepository
import com.example.skeleton.data.repository.impl.PlayerRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    single<MusicRepository> { MusicRepositoryImpl(context = androidContext()) }

    single<PlayerRepository> { PlayerRepositoryImpl(context = androidContext()) }
}