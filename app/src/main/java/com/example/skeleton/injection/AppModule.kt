package com.example.skeleton.injection

import org.koin.dsl.module

val appModule = module {
    includes(
        databaseModule,
        datastoreModule,
        repositoryModule,
        viewModelModule,
        networkModule,
        localeModule)
}