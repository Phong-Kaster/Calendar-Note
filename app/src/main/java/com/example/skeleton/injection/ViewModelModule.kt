package com.example.skeleton.injection



import com.example.skeleton.ui.fragment.alarm_editor.AlarmEditorViewModel
import com.example.skeleton.ui.fragment.alarms.AlarmsViewModel
import com.example.skeleton.ui.fragment.calendar.CalendarViewModel
import com.example.skeleton.ui.fragment.home.HomeViewModel
import com.example.skeleton.ui.fragment.note.NoteViewModel
import com.example.skeleton.ui.fragment.setting.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    // Setting View Model
    viewModel { SettingViewModel(settingRepository = get()) }

    // Home View Model
    viewModel { HomeViewModel(noteRepository = get()) }

    // Note View Model
    viewModel { NoteViewModel(noteRepository = get()) }

    // Calendar View Model — the clock is left at its default (the device's own), which is the
    // only reason that parameter exists as a parameter: so a test can pass a different one.
    viewModel { CalendarViewModel(noteRepository = get()) }

    // Alarms View Model — reads the alarm store's live list so the screen stays current on its
    // own, and re-arms the whole list on the one occasion the system has quietly cancelled it all:
    // the exact-alarm permission being granted back after having been revoked.
    viewModel { AlarmsViewModel(alarmRepository = get(), alarmScheduler = get()) }

    // Alarm Editor View Model — writes through the same store. Without this registration the editor
    // would compile perfectly and crash the moment a user opened it.
    viewModel { AlarmEditorViewModel(alarmRepository = get()) }
}