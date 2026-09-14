package com.example.skeleton.data.mapper

import com.example.skeleton.data.database.local.entity.AlarmEntity
import com.example.skeleton.domain.enums.AlarmRepeatMode
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.scheduler.toRepeatDaySet
import com.example.skeleton.domain.scheduler.toRepeatDaysBitmask

/*
 * --- The only place a stored alarm becomes a real alarm (simple story) ---
 *
 * Most fields are a straight copy across, but `repeatMode` and `repeatDays` are not — the table
 * stores an enum as its bare `String` name and a set of weekdays as one packed `Int`, because SQLite
 * has neither shape natively. This is the one place that translation goes, and nothing else in the
 * app (not the DAO, not the repository, not a screen) is allowed to do it by hand.
 *
 * Both directions live here, together, so they can never drift apart.
 */

/**
 * Turns a stored row into the alarm the app works with.
 *
 * @author Phong-Kaster
 */
fun AlarmEntity.toDomain(): Alarm {
    return Alarm(
        id = id,
        message = message,
        hourOfDay = hourOfDay,
        minute = minute,
        enabled = enabled,
        repeatMode = AlarmRepeatMode.valueOf(repeatMode),
        repeatDays = repeatDays.toRepeatDaySet(),
        createdAt = createdAt,
    )
}

/**
 * Turns an alarm into the row the database stores.
 *
 * @author Phong-Kaster
 */
fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        message = message,
        hourOfDay = hourOfDay,
        minute = minute,
        enabled = enabled,
        repeatMode = repeatMode.name,
        repeatDays = repeatDays.toRepeatDaysBitmask(),
        createdAt = createdAt,
    )
}
