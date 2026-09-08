package com.example.skeleton.injection

import com.example.skeleton.ui.util.LocaleManager
import org.koin.core.module.Module
import org.koin.dsl.module

val localeModule = module {
    single { LocaleManager() }
}
