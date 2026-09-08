package com.example.skeleton.injection

import com.example.skeleton.data.repository.impl.PostRepositoryImpl
import com.example.skeleton.data.repository.impl.SettingRepositoryImpl
import com.example.skeleton.data.repository.impl.UserActionRepositoryImpl
import com.example.skeleton.domain.repository.PostRepository
import com.example.skeleton.domain.repository.SettingRepository
import com.example.skeleton.domain.repository.UserActionRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<SettingRepository> { SettingRepositoryImpl(settingDatastore = get()) }

    single<UserActionRepository> { UserActionRepositoryImpl(dao = get()) }

    single<PostRepository> { PostRepositoryImpl(api = get(), dao = get()) }
}