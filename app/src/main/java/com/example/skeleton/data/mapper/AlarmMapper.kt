package com.example.skeleton.data.mapper

import com.example.skeleton.data.database.local.entity.AlarmEntity
import com.example.skeleton.domain.model.Alarm

/*
 * --- The only place a stored alarm becomes a real alarm (simple story) ---
 *
 * Today the two shapes hold the same fields, so both functions are a straight copy across. That is
 * not a reason to skip the file: the moment the database and the domain disagree about anything —
 * a stored minute-of-day against an hour and a minute, say — this is the one place the translation
 * goes, and nothing else in the app (not the DAO, not the repository, not a screen) is allowed to
 * do it by hand.
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
        createdAt = createdAt,
    )
}
