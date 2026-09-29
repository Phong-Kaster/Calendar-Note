package com.example.skeleton.injection

import org.koin.dsl.module

/**
 * Root Koin module: gathers the repository and view model modules.
 *
 * @author Phong-Kaster
 */
val appModule = module {
    includes(
        repositoryModule,
        viewModelModule,
    )
}
