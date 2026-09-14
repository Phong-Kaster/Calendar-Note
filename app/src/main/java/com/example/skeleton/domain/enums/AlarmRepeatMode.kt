package com.example.skeleton.domain.enums

/**
 * How often an alarm repeats once it is armed.
 *
 * This decides two things, not one: what [com.example.skeleton.domain.scheduler.nextFireTime] treats
 * as a valid day to fire on, and whether `AlarmReceiver` re-arms the alarm after it fires. The two
 * stay together in one enum rather than two flags because they can never disagree — an alarm that is
 * [ONE_TIME] has no "next valid day" to compute in the first place.
 *
 * @author Phong-Kaster
 */
enum class AlarmRepeatMode {

    /** Fires once, at its next occurrence, and is not re-armed afterwards. */
    ONE_TIME,

    /** Fires at the same time every day. The behaviour every alarm had before this enum existed. */
    DAILY,

    /**
     * Fires only on the weekdays named in [com.example.skeleton.domain.model.Alarm.repeatDays].
     *
     * An empty [com.example.skeleton.domain.model.Alarm.repeatDays] under this mode means "no day
     * qualifies" — the alarm is saved and shown as armed, but nothing is ever scheduled for it until
     * at least one weekday is picked. That is a state the editor can produce (a user can switch to
     * Custom before ticking anything) and not one the scheduler is allowed to crash on.
     */
    CUSTOM,
}
