package com.example.skeleton.injection

import com.example.skeleton.data.datastore.SettingDatastore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val datastoreModule = module {

    // Setting Datastore
    single { SettingDatastore(context = androidContext()) }
}