package com.example.skeleton.data.repository

import com.example.skeleton.common.Outcome
import com.example.skeleton.data.database.local.dao.AlarmDao
import com.example.skeleton.data.database.local.entity.AlarmEntity
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mapper.toEntity
import com.example.skeleton.data.repository.impl.AlarmRepositoryImpl
import com.example.skeleton.data.scheduler.BrokenAlarmScheduler
import com.example.skeleton.data.scheduler.FakeAlarmScheduler
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.model.BlankAlarmMessageException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Holds [AlarmRepositoryImpl] to the promises its interface makes: alarms come out earliest time of
 * day first whatever order they went in, an alarm with nothing written on it never goes in at all,
 * a read has three answers rather than two, **a delete that removed nothing is reported as a failure
 * rather than as a success**, and **nothing that goes wrong inside ever leaves as an exception**.
 *
 * The DAO underneath is a fake — a list in memory, no Room, no Android, no device. That is the point
 * of the fake rather than a convenience. `AlarmDao.observeAll()` has **no `ORDER BY` at all**: the
 * ordering is the repository's own comparator, and it is written in Kotlin precisely so that an
 * ordinary JVM test can check it. Nothing in this project can run SQLite — there is no emulator and
 * no Robolectric — so a promise that lived in a SQL string would be uncheckable here. [FakeAlarmDao]
 * therefore hands back rows **in the order it was given them**, deliberately jumbled by the tests
 * below; a repository leaning on the database to sort for it would fail them.
 *
 * The fail-soft contract earns a second fake, [BrokenAlarmDao], whose every call throws. It is the
 * only way to reach the impl's catch blocks: the real failure is a corrupt database file or a disk
 * that will not read, and nothing here can produce either.
 *
 * The last section is about the promise that has no screen to show it: **the phone's own list of
 * pending alarms has to agree with the table.** `AlarmManager` is invisible to a test on this
 * toolchain, so the store depends on the `AlarmScheduler` interface and these tests hand it
 * [FakeAlarmScheduler] — the one that simply writes down what it was asked to do — and
 * [BrokenAlarmScheduler], the one where arming fails. Both live in `AlarmSchedulingTest` rather than
 * here, because there must be exactly one of each.
 *
 * The clock is a constructor parameter and every test that cares passes [CLOCK], a fixed one. A test
 * that read the real clock could not assert on a timestamp at all.
 *
 * @author Phong-Kaster
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmRepositoryImplTest {

    // ---------- The order the list comes out in ----------

    @Test
    fun `alarms come out earliest first even when the dao hands them over jumbled`() = runTest {
        val dao = FakeAlarmDao(
            rows = listOf(
                entity(id = 1L, hourOfDay = 21, minute = 30),
                entity(id = 2L, hourOfDay = 7, minute = 0),
                entity(id = 3L, hourOfDay = 12, minute = 15),
                entity(id = 4L, hourOfDay = 7, minute = 45),
            ),
        )
        val repository = AlarmRepositoryImpl(alarmDao = dao, alarmScheduler = FakeAlarmScheduler())

        val alarms = repository.alarmsFlow.first()

        assertEquals(listOf(2L, 4L, 3L, 1L), alarms.map { alarm -> alarm.id })
    }

    @Test
    fun `the minute is what separates two alarms in the same hour`() = runTest {
        // The half of the ordering an hour-only comparison would get wrong, and it would look
        // perfect on any list whose alarms happened to be in different hours. 07:45 before 07:05
        // is a list that reads as unsorted to the one person looking at it — the user.
        val dao = FakeAlarmDao(
            rows = listOf(
                entity(id = 1L, hourOfDay = 7, minute = 45),
                entity(id = 2L, hourOfDay = 7, minute = 5),
            ),
        )
        val repository = AlarmRepositoryImpl(alarmDao = dao, alarmScheduler = FakeAlarmScheduler())

        val alarms = repository.alarmsFlow.first()

        assertEquals(listOf(2L, 1L), alarms.map { alarm -> alarm.id })
    }

    @Test
    fun `alarms set for the same minute keep a stable order`() = runTest {
        // Without the id tiebreaker these three are equal as far as the comparator is concerned, and
        // a sort is free to return them in any order it likes from one emission to the next. A list
        // that reshuffles itself while the user is looking at it is the kind of bug that reproduces
        // once a week and never in a test — unless the tiebreaker is asserted, which is this.
        val dao = FakeAlarmDao(
            rows = listOf(
                entity(id = 9L, hourOfDay = 8, minute = 0),
                entity(id = 7L, hourOfDay = 8, minute = 0),
                entity(id = 8L, hourOfDay = 8, minute = 0),
            ),
        )
        val repository = AlarmRepositoryImpl(alarmDao = dao, alarmScheduler = FakeAlarmScheduler())

        val alarms = repository.alarmsFlow.first()

        assertEquals(listOf(7L, 8L, 9L), alarms.map { alarm -> alarm.id })
    }

    @Test
    fun `an empty table produces an empty list, not a failure`() = runTest {
        val repository = AlarmRepositoryImpl(
            alarmDao = FakeAlarmDao(rows = emptyList()),
            alarmScheduler = FakeAlarmScheduler(),
        )

        val alarms = repository.alarmsFlow.first()

        assertEquals(emptyList<Alarm>(), alarms)
    }

    @Test
    fun `a database that will not be read shows an empty list instead of crashing the screen`() =
        runTest {
            // `alarmsFlow` is collected by a ViewModel that catches nothing, so an exception escaping
            // here would take the app down rather than showing a wrong-but-survivable empty list.
            val repository = AlarmRepositoryImpl(
                alarmDao = BrokenAlarmDao(),
                alarmScheduler = FakeAlarmScheduler(),
                clock = CLOCK,
            )

            val alarms = repository.alarmsFlow.first()

            assertEquals(emptyList<Alarm>(), alarms)
        }

    @Test
    fun `a stored alarm survives the trip out of the database`() = runTest {
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L, hourOfDay = 21, minute = 30)))
        val repository = AlarmRepositoryImpl(alarmDao = dao, alarmScheduler = FakeAlarmScheduler())

        val alarm = repository.alarmsFlow.first().single()

        assertEquals(5L, alarm.id)
        assertEquals("Take the bread out of the freezer", alarm.message)
        assertEquals(21, alarm.hourOfDay)
        assertEquals(30, alarm.minute)
        assertTrue(alarm.enabled)
        assertEquals(1_000L, alarm.createdAt)
    }

    @Test
    fun `an alarm survives the round trip back into the database and out again`() = runTest {
        // Both mapper directions at once. They copy field for field today, so the mistake they are
        // guarding against is a future one: an hour and a minute swapped on the way in would file
        // 07:30 as 30:07 and nothing else in the app would notice until an alarm never went off.
        val alarm = Alarm(
            id = 42L,
            message = "Take the bread out of the freezer",
            hourOfDay = 7,
            minute = 30,
            enabled = false,
            createdAt = 1_000L,
        )

        assertEquals(alarm, alarm.toEntity().toDomain())
    }

    @Test
    fun `the list keeps up with an alarm written after it was already being read`() = runTest {
        // The reason this returns a `Flow` at all, and the reason the Alarms screen shows a new
        // alarm the moment the editor closes. A stray `.first()` or `.take(1)` inside the repository
        // would leave the user staring at a list that does not contain what they just wrote — and
        // every other test in this file would stay green, because every other test reads one
        // emission and stops.
        //
        // Every dispatcher here is the test's own, unconfined: the flow is `flowOn(ioDispatcher)`
        // and the default is the real `Dispatchers.IO`, so without injecting one the collector would
        // run on a background thread and this test would pass or fail on timing.
        val dao = FakeAlarmDao(rows = emptyList())
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val emissions = mutableListOf<List<Alarm>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.alarmsFlow.collect { alarms -> emissions += alarms }
        }
        repository.save(alarm = Alarm.draft(message = "Leave for the dentist"))

        assertEquals(
            listOf(emptyList(), listOf("Leave for the dentist")),
            emissions.map { alarms -> alarms.map { alarm -> alarm.message } },
        )
    }

    // ---------- Saving: the store owns the clock ----------

    @Test
    fun `a brand-new alarm is stamped with the clock the store was given`() = runTest {
        // Not with `System.currentTimeMillis()`, which is what makes this assertable at all. The
        // clock is a constructor seam for exactly this reason: a store that read the real clock
        // could only be checked with "the stamp is roughly now", which passes against a store that
        // does not stamp at all if the fixture happened to be built a moment ago.
        val dao = FakeAlarmDao(rows = emptyList())
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.save(alarm = Alarm.draft(message = "Leave for the dentist"))

        assertTrue(outcome is Outcome.Success)
        assertEquals(NOW_MILLIS, repository.alarmsFlow.first().single().createdAt)
    }

    @Test
    fun `an alarm that already carries a creation time keeps it`() = runTest {
        // The half a fresh database cannot show. If `save` stamped unconditionally, every alarm in
        // the app would report having been created the moment it was last edited — and nothing on
        // any screen would look wrong, because no screen draws the stamp.
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L, createdAt = 1_000L)))
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val existing = repository.storedAlarm(id = 5L)
        val outcome = repository.save(alarm = existing.copy(message = "Leave for the dentist, early"))

        assertTrue(outcome is Outcome.Success)
        val stored = repository.storedAlarm(id = 5L)
        assertEquals(1_000L, stored.createdAt)
        assertEquals("Leave for the dentist, early", stored.message)
        // The edit changed an alarm; it did not add one. An id dropped on the way in would land as a
        // second row here rather than replacing the first.
        assertEquals(1, repository.alarmsFlow.first().size)
    }

    @Test
    fun `the time an alarm was given is the time it is stored under`() = runTest {
        val dao = FakeAlarmDao(rows = emptyList())
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        repository.save(
            alarm = Alarm.draft(message = "Leave for the dentist", hourOfDay = 21, minute = 30),
        )

        val stored = repository.alarmsFlow.first().single()
        assertEquals(21, stored.hourOfDay)
        assertEquals(30, stored.minute)
    }

    // ---------- Saving: an alarm has to say something ----------

    @Test
    fun `an alarm with a blank message is refused, and nothing is written`() = runTest {
        val dao = FakeAlarmDao(rows = emptyList())
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.save(alarm = Alarm.draft(message = ""))

        assertTrue(outcome is Outcome.Error)
        // The refusal has to mean the row never appeared. A `save` that reported an error *and*
        // wrote the row would pass an assertion on the return value alone.
        assertEquals(emptyList<Alarm>(), repository.alarmsFlow.first())
    }

    @Test
    fun `a message of nothing but spaces is refused too`() = runTest {
        // `isBlank`, not `isEmpty`. A space bar pressed twice is still an alarm that says nothing
        // when it goes off, and it is far easier to produce by accident than a truly empty field.
        val repository = AlarmRepositoryImpl(
            alarmDao = FakeAlarmDao(rows = emptyList()),
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.save(alarm = Alarm.draft(message = "   "))

        assertTrue(outcome is Outcome.Error)
    }

    @Test
    fun `a refusal says it is the blank-message rule, not a write that went wrong`() = runTest {
        // The distinction the screen above depends on, and the reason it is carried by **type**.
        // Both a refusal and a broken disk come back as `Outcome.Error`, and the editor has to say
        // opposite things about them: a failed write is worth another tap, a refusal never is —
        // the message is as blank as it was a moment ago. A screen that told them apart by reading
        // `message` would break the next time somebody rephrased that line, so the tag is what is
        // asserted here and the wording is deliberately not asserted at all.
        val repository = AlarmRepositoryImpl(
            alarmDao = FakeAlarmDao(rows = emptyList()),
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.save(alarm = Alarm.draft(message = ""))

        assertTrue((outcome as Outcome.Error).throwable is BlankAlarmMessageException)
    }

    @Test
    fun `a write the database refuses is not dressed up as a refusal`() = runTest {
        // The other half of the line above, and the half with teeth: tagging *every* error as a
        // refusal would pass the test before this one and would tell a user whose disk is failing
        // that they forgot to write anything. The message here is perfectly good, so the rule has no
        // business firing at all.
        val repository = AlarmRepositoryImpl(
            alarmDao = BrokenAlarmDao(),
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.save(alarm = Alarm.draft(message = "Leave for the dentist"))

        assertTrue(outcome is Outcome.Error)
        assertFalse((outcome as Outcome.Error).throwable is BlankAlarmMessageException)
    }

    @Test
    fun `an existing alarm cannot be edited into saying nothing either`() = runTest {
        // The rule covers every write path, not just creation. Clearing the message of a stored
        // alarm is the same violation as creating one blank, and the stored row must survive it.
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L)))
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val existing = repository.storedAlarm(id = 5L)
        val outcome = repository.save(alarm = existing.copy(message = ""))

        assertTrue(outcome is Outcome.Error)
        assertEquals("Take the bread out of the freezer", repository.storedAlarm(id = 5L).message)
    }

    // ---------- Reading one alarm: three answers, not two ----------

    @Test
    fun `asking for an alarm that is not there is a successful read of nothing, not an error`() =
        runTest {
            // The whole reason `getAlarm` returns an `Outcome`. "I looked and it is not there" is an
            // ordinary answer a screen can act on — the alarm was removed. Reporting it as an error
            // would make it indistinguishable from a database that would not answer.
            val repository = AlarmRepositoryImpl(
                alarmDao = FakeAlarmDao(rows = emptyList()),
                alarmScheduler = FakeAlarmScheduler(),
                clock = CLOCK,
            )

            val outcome = repository.getAlarm(id = 404L)

            assertTrue(outcome is Outcome.Success)
            assertNull((outcome as Outcome.Success).data)
        }

    @Test
    fun `asking for an alarm that is there answers with it`() = runTest {
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L, hourOfDay = 21, minute = 30)))
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.getAlarm(id = 5L)

        assertTrue(outcome is Outcome.Success)
        val alarm = (outcome as Outcome.Success).data
        assertEquals(5L, alarm?.id)
        assertEquals(21, alarm?.hourOfDay)
        assertEquals(30, alarm?.minute)
    }

    @Test
    fun `a read the database refuses is an error, not an empty answer`() = runTest {
        // The third answer, and the one that used to be invisible in the notes store: a thrown read
        // came back as `null` — the same value as "no such row" — and the editor opened blank, then
        // filed a *second* row beside the original on save. The same shape is used here so the same
        // mistake cannot be made twice.
        val repository = AlarmRepositoryImpl(
            alarmDao = BrokenAlarmDao(),
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.getAlarm(id = 5L)

        assertTrue(outcome is Outcome.Error)
    }

    // ---------- Switching an alarm off: an ordinary edit, and it has to stick ----------

    @Test
    fun `an alarm switched off stays switched off in the store`() = runTest {
        // There is no `setEnabled` in this store and there is not supposed to be one: being armed is
        // a field on the alarm, so flipping it is an ordinary save. What this checks is that the
        // ordinary save carries the field — a `toEntity` that dropped it, or a table that never
        // wrote the column, would leave every alarm armed and nothing would say so until one went
        // off at six in the morning.
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L, enabled = true)))
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val existing = repository.storedAlarm(id = 5L)
        val outcome = repository.save(alarm = existing.copy(enabled = false))

        assertTrue(outcome is Outcome.Success)
        assertFalse(repository.storedAlarm(id = 5L).enabled)
        // Switched off, not replaced: everything else about the alarm survives the trip.
        assertEquals("Take the bread out of the freezer", repository.storedAlarm(id = 5L).message)
        assertEquals(1_000L, repository.storedAlarm(id = 5L).createdAt)
        assertEquals(1, repository.alarmsFlow.first().size)
    }

    @Test
    fun `a switched-off alarm is still in the list`() = runTest {
        // Off is not gone. A store that filtered disabled alarms out of its own list would make
        // switching one off indistinguishable from deleting it, and the user would have no way back.
        val dao = FakeAlarmDao(
            rows = listOf(entity(id = 1L, enabled = false), entity(id = 2L, enabled = true)),
        )
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val alarms = repository.alarmsFlow.first()

        assertEquals(listOf(1L, 2L), alarms.map { alarm -> alarm.id })
    }

    // ---------- Deleting: a write that matched nothing is not a success ----------

    @Test
    fun `deleting an alarm takes it out of the store`() = runTest {
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L), entity(id = 6L, hourOfDay = 9)))
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.delete(alarm = repository.storedAlarm(id = 5L))

        assertTrue(outcome is Outcome.Success)
        // The one that was asked for, and only that one. A delete that cleared the table would pass
        // an assertion on the removed alarm alone.
        assertEquals(listOf(6L), repository.alarmsFlow.first().map { alarm -> alarm.id })
    }

    @Test
    fun `the list drops an alarm deleted while it was already being read`() = runTest {
        // The same reason `alarmsFlow` is a flow at all, from the other direction: the row has to
        // leave the screen without anybody asking the store again. Every dispatcher here is the
        // test's own, unconfined, or the collector would run on a real background thread and this
        // test would pass or fail on timing.
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L)))
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val emissions = mutableListOf<List<Long>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.alarmsFlow.collect { alarms -> emissions += alarms.map { it.id } }
        }
        repository.delete(alarm = repository.storedAlarm(id = 5L))

        assertEquals(listOf(listOf(5L), emptyList<Long>()), emissions)
    }

    @Test
    fun `a delete that matched no row is an error, not a quiet success`() = runTest {
        // **The reason `AlarmDao.delete` returns an `Int` at all.** Room matches on the primary key
        // and is perfectly content to match nothing and report nothing wrong — so without the count
        // this would come back as a success and the screen would say "Alarm deleted" about a row
        // that was never there. The id is a plausible one, not the unsaved sentinel: this is the
        // ordinary race, an alarm already gone by the time the confirmation was answered.
        val repository = AlarmRepositoryImpl(
            alarmDao = FakeAlarmDao(rows = emptyList()),
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.delete(
            alarm = Alarm(
                id = 404L,
                message = "Take the bread out of the freezer",
                hourOfDay = 8,
                minute = 0,
                createdAt = 1_000L,
            ),
        )

        assertTrue(outcome is Outcome.Error)
    }

    @Test
    fun `an alarm that was never stored cannot be deleted, and the table is never asked`() = runTest {
        // A draft has no row to remove. The refusal happens above the DAO on purpose — `deleteCount`
        // is what says so, because "the table still has the same rows" would also be true of a
        // delete that reached the table and matched nothing.
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L)))
        val repository = AlarmRepositoryImpl(
            alarmDao = dao,
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.delete(alarm = Alarm.draft(message = "Leave for the dentist"))

        assertTrue(outcome is Outcome.Error)
        assertEquals(0, dao.deleteCount)
        assertEquals(1, repository.alarmsFlow.first().size)
    }

    @Test
    fun `a delete the database refuses comes back as an error rather than as an exception`() = runTest {
        // The fail-soft contract, on the one method that did not exist when it was written down. An
        // exception here would leave the confirmation sheet open over a crashed screen.
        val repository = AlarmRepositoryImpl(
            alarmDao = BrokenAlarmDao(),
            alarmScheduler = FakeAlarmScheduler(),
            clock = CLOCK,
        )

        val outcome = repository.delete(
            alarm = Alarm(
                id = 5L,
                message = "Take the bread out of the freezer",
                hourOfDay = 8,
                minute = 0,
                createdAt = 1_000L,
            ),
        )

        assertTrue(outcome is Outcome.Error)
    }

    // ---------- Mirroring the schedule ----------

    @Test
    fun `a brand-new alarm is armed under the id the table gave it, not under zero`() = runTest {
        // The defect here is completely invisible on screen. A draft carries `Alarm.UNSAVED_ID` —
        // zero — and the scheduler refuses to arm an alarm with no id, because zero is an address
        // every unsaved alarm in the app would share. So a save that armed the alarm it was *handed*
        // instead of the row that was *written* would arm nothing at all, for every alarm the user
        // ever creates, while the list showed each one exactly as expected.
        //
        // The table already holds row 7, so the id asserted below can only have come out of the
        // insert: it is neither the zero that went in nor the first number a counter would produce.
        val scheduler = FakeAlarmScheduler()
        val dao = FakeAlarmDao(rows = listOf(entity(id = 7L)))
        val repository = AlarmRepositoryImpl(alarmDao = dao, alarmScheduler = scheduler, clock = CLOCK)

        val outcome = repository.save(alarm = Alarm.draft(message = "Leave for the dentist"))

        assertTrue(outcome is Outcome.Success)
        assertEquals(listOf(8L), scheduler.scheduled.map { alarm -> alarm.id })
    }

    @Test
    fun `an alarm saved switched off is cancelled and never armed`() = runTest {
        // "Cancel it" and "leave it alone" look identical on a phone with nothing pending, and could
        // not be more different in real use: an alarm the user has just switched off almost always
        // has one already armed, so a save that merely skipped arming would leave the phone going
        // off tomorrow morning for an alarm whose switch reads OFF on screen.
        val scheduler = FakeAlarmScheduler()
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L, enabled = true)))
        val repository = AlarmRepositoryImpl(alarmDao = dao, alarmScheduler = scheduler, clock = CLOCK)

        val existing = repository.storedAlarm(id = 5L)
        val outcome = repository.save(alarm = existing.copy(enabled = false))

        assertTrue(outcome is Outcome.Success)
        assertEquals(listOf(5L), scheduler.cancelled)
        assertEquals(emptyList<Alarm>(), scheduler.scheduled)
    }

    @Test
    fun `editing an alarm cancels the old schedule before arming the new one`() = runTest {
        // The order is the entire assertion, and it is why the fake keeps one interleaved list at
        // all: "cancel the old one, then arm the new one" and "arm the new one, then cancel it
        // again" leave identical marks in the two separate lists and have opposite effects on the
        // phone — the second leaves the user with no alarm whatsoever.
        //
        // What it is guarding: moving an alarm from 08:00 to 21:30 arms a fresh one, and without the
        // cancel the user now has *two* pending, one of them for a time no longer written anywhere
        // and not shown on any screen.
        val scheduler = FakeAlarmScheduler()
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L, hourOfDay = 8, minute = 0)))
        val repository = AlarmRepositoryImpl(alarmDao = dao, alarmScheduler = scheduler, clock = CLOCK)

        val existing = repository.storedAlarm(id = 5L)
        val outcome = repository.save(alarm = existing.copy(hourOfDay = 21, minute = 30))

        assertTrue(outcome is Outcome.Success)
        assertEquals(listOf("cancel(5)", "schedule(5)"), scheduler.calls)
    }

    @Test
    fun `deleting an alarm also cancels it on the phone`() = runTest {
        // **The worst thing this feature can do, and the reason the whole scheduling seam exists.** A
        // row taken out of the table while its alarm stays armed still goes off, still shows its
        // notification, and — because the receiver arms tomorrow's copy as it fires — goes off again
        // every morning afterwards, for an alarm the user cannot see in the list and therefore
        // cannot delete a second time. No screen anywhere would show it; this assertion is the only
        // thing in the project that can.
        val scheduler = FakeAlarmScheduler()
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L), entity(id = 6L, hourOfDay = 9)))
        val repository = AlarmRepositoryImpl(alarmDao = dao, alarmScheduler = scheduler, clock = CLOCK)

        val outcome = repository.delete(alarm = repository.storedAlarm(id = 5L))

        assertTrue(outcome is Outcome.Success)
        // The one that was deleted, and only that one: alarm 6 is still in the list and must still be
        // pending. Asserting the whole call list is what says so.
        assertEquals(listOf("cancel(5)"), scheduler.calls)
    }

    @Test
    fun `a delete that matched no row cancels nothing`() = runTest {
        // Nothing changed in the table, so nothing on the phone may change either. The id is a
        // plausible one rather than the unsaved sentinel — this is the ordinary race, an alarm
        // already gone by the time the confirmation was answered — and cancelling on the way past
        // would mean this store reaching out and disarming whatever happens to be filed under 404
        // today, which after a row id is reused is somebody else's alarm.
        val scheduler = FakeAlarmScheduler()
        val repository = AlarmRepositoryImpl(
            alarmDao = FakeAlarmDao(rows = emptyList()),
            alarmScheduler = scheduler,
            clock = CLOCK,
        )

        val outcome = repository.delete(
            alarm = Alarm(
                id = 404L,
                message = "Take the bread out of the freezer",
                hourOfDay = 8,
                minute = 0,
                createdAt = 1_000L,
            ),
        )

        assertTrue(outcome is Outcome.Error)
        assertEquals(emptyList<String>(), scheduler.calls)
    }

    @Test
    fun `an alarm refused for a blank message never reaches the scheduler`() = runTest {
        // The refusal happens before the table is touched, so there is no row for anything to mirror
        // and the alarm on the phone is still the right one. A store that cancelled on its way out
        // of a refusal would disarm a perfectly good alarm the user had merely failed to edit: row 5
        // would still be in the list, its switch would still read ON, and it would never ring again.
        val scheduler = FakeAlarmScheduler()
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L)))
        val repository = AlarmRepositoryImpl(alarmDao = dao, alarmScheduler = scheduler, clock = CLOCK)

        val existing = repository.storedAlarm(id = 5L)
        val outcome = repository.save(alarm = existing.copy(message = ""))

        assertTrue(outcome is Outcome.Error)
        assertEquals(emptyList<String>(), scheduler.calls)
    }

    @Test
    fun `switching an alarm back on arms it again, for the time it is actually set for`() = runTest {
        // The other direction of the switch, and the half that is easy to leave out: a store that
        // only ever cancelled would let a user turn an alarm off and on again and leave them with an
        // alarm that reads ON and never rings.
        //
        // The hour and minute are asserted on the alarm that reached the scheduler, not just its id,
        // because arming the right row *at the wrong time* is the same failure wearing a disguise.
        // It is the scheduler, not this store, that turns hour-and-minute into a fire instant (this
        // store's own `clock` only stamps `createdAt`) — which is exactly why this test stops at "the
        // right alarm reached the scheduler" rather than asserting a computed instant; that half is
        // `AlarmSchedulingTest`'s and `NextFireTimeTest`'s job.
        val scheduler = FakeAlarmScheduler()
        val dao = FakeAlarmDao(
            rows = listOf(entity(id = 5L, hourOfDay = 21, minute = 30, enabled = false)),
        )
        val repository = AlarmRepositoryImpl(alarmDao = dao, alarmScheduler = scheduler, clock = CLOCK)

        val existing = repository.storedAlarm(id = 5L)
        val outcome = repository.save(alarm = existing.copy(enabled = true))

        assertTrue(outcome is Outcome.Success)
        val armed = scheduler.scheduled.single()
        assertEquals(5L, armed.id)
        assertEquals(21, armed.hourOfDay)
        assertEquals(30, armed.minute)
        assertTrue(armed.enabled)
    }

    @Test
    fun `a save whose alarm could not be armed is still a success, and the row is still stored`() =
        runTest {
            // The database is the truth and the phone's pending alarms are a best-effort copy of it,
            // so the write's answer cannot depend on the copy. The real failure this stands in for is
            // a device withdrawing the exact-alarm permission between the editor opening and the save
            // landing — and the user's only possible response to a false "could not save" is to tap
            // save again, which writes the very same alarm a second time and fails identically.
            val dao = FakeAlarmDao(rows = emptyList())
            val repository = AlarmRepositoryImpl(
                alarmDao = dao,
                alarmScheduler = BrokenAlarmScheduler(),
                clock = CLOCK,
            )

            val outcome = repository.save(alarm = Alarm.draft(message = "Leave for the dentist"))

            assertTrue(outcome is Outcome.Success)
            // And "success" has to mean the row really is there. A `save` that swallowed the
            // scheduler's failure *and* lost the write would pass an assertion on the outcome alone.
            assertEquals(
                listOf("Leave for the dentist"),
                repository.alarmsFlow.first().map { alarm -> alarm.message },
            )
        }

    @Test
    fun `a delete whose alarm could not be cancelled still removes the row and still succeeds`() =
        runTest {
            // The same contract from the other end. The row has gone, and saying so is the truth even
            // though the phone kept its pending alarm. Reporting an error here would be worse than
            // the stale schedule it complains about: the alarm would already be out of the list, the
            // screen would say the delete failed, and the user would have nothing left to try.
            val dao = FakeAlarmDao(rows = listOf(entity(id = 5L)))
            val repository = AlarmRepositoryImpl(
                alarmDao = dao,
                alarmScheduler = BrokenAlarmScheduler(),
                clock = CLOCK,
            )

            val outcome = repository.delete(alarm = repository.storedAlarm(id = 5L))

            assertTrue(outcome is Outcome.Success)
            assertEquals(emptyList<Alarm>(), repository.alarmsFlow.first())
        }

    private fun entity(
        id: Long,
        hourOfDay: Int = 8,
        minute: Int = 0,
        enabled: Boolean = true,
        createdAt: Long = 1_000L,
    ): AlarmEntity = AlarmEntity(
        id = id,
        message = "Take the bread out of the freezer",
        hourOfDay = hourOfDay,
        minute = minute,
        enabled = enabled,
        createdAt = createdAt,
    )

    private companion object {

        /** The day every test in this file pretends it is. */
        private val TODAY: LocalDate = LocalDate.of(2026, 3, 14)

        /**
         * A clock stopped mid-morning on [TODAY].
         *
         * Mid-morning rather than midnight so that no assertion here can pass or fail on which side
         * of a day boundary the instant landed.
         */
        private val CLOCK: Clock = Clock.fixed(
            TODAY.atStartOfDay(ZoneOffset.UTC).plusHours(9L).toInstant(),
            ZoneOffset.UTC,
        )

        /** What [CLOCK] reads in epoch milliseconds — the stamp every new alarm here should get. */
        private val NOW_MILLIS: Long = CLOCK.millis()
    }
}

/**
 * An alarms table that is really just a list held in memory.
 *
 * [observeAll] returns the rows **in the order they were handed to the constructor**, on purpose. A
 * fake that sorted would be agreeing with the code under test instead of checking it — and here that
 * matters more than usual, because the real `AlarmDao.observeAll()` has no `ORDER BY` at all and the
 * entire ordering promise belongs to the repository's comparator.
 *
 * It is **live**, the way Room's is: a row written through [upsert] shows up in a collector that is
 * already running. A snapshot would have been easier and would have hidden the mistake that matters
 * most to the Alarms screen — a list that never updates after a save.
 *
 * It does emulate one more thing Room does, because a promise now depends on it: **`autoGenerate`.**
 * A row arriving with [Alarm.UNSAVED_ID] is given a fresh id and that id is handed back, exactly as
 * `@Insert` does. A fake that kept the zero it was given would let a repository arm a brand-new alarm
 * under request code 0 — an address every unsaved alarm shares — and the test that catches it could
 * not tell the difference.
 *
 * @param rows the rows the fake table starts with.
 * @author Phong-Kaster
 */
private class FakeAlarmDao(rows: List<AlarmEntity>) : AlarmDao {

    private val storedRows = MutableStateFlow(rows)

    /**
     * The id the next inserted row gets, starting above every id the table was handed.
     *
     * Starting above them rather than at `1` so that a fake built with existing rows cannot hand a
     * brand-new alarm an id that is already taken — which would silently replace a row instead of
     * adding one, and would make a test about a *new* alarm quietly about an edit.
     */
    private var nextId: Long = (rows.maxOfOrNull { row -> row.id } ?: 0L) + 1L

    /**
     * How many times [delete] was asked to remove something.
     *
     * It is here for the tests about a delete that must **not** reach the table at all — an alarm
     * that was never stored. "The table still has the same rows" is a weaker statement than "the
     * table was never asked", because a delete that matched nothing leaves the rows alone too.
     */
    var deleteCount: Int = 0
        private set

    override fun observeAll(): Flow<List<AlarmEntity>> = storedRows

    override suspend fun getById(id: Long): AlarmEntity? =
        storedRows.value.firstOrNull { row -> row.id == id }

    /**
     * Stores one row and answers with the id it was stored under — a fresh one for an insert, the
     * row's own for a replace. That returned id is what the repository arms the alarm with.
     */
    override suspend fun upsert(alarm: AlarmEntity): Long {
        val rowId = if (alarm.id == Alarm.UNSAVED_ID) nextId++ else alarm.id
        val stored = alarm.copy(id = rowId)

        storedRows.value = storedRows.value.filterNot { row -> row.id == rowId } + stored
        return rowId
    }

    /**
     * Removes the row with this id and reports how many went — **exactly the way Room does**,
     * including the part that matters: matching nothing is `0` and not a complaint.
     *
     * A fake that threw, or that returned 1 regardless, would agree with the repository instead of
     * checking it, and the "the screen announced a deletion that never happened" defect would have
     * no test that could see it.
     */
    override suspend fun delete(alarm: AlarmEntity): Int {
        deleteCount++

        val remaining = storedRows.value.filterNot { row -> row.id == alarm.id }
        val removed = storedRows.value.size - remaining.size
        storedRows.value = remaining
        return removed
    }
}

/**
 * An alarms table where every single thing fails.
 *
 * It exists because the repository's fail-soft promise — *never throw at the screen above you* — is
 * only a promise until something actually throws. There is no other way to reach those catch blocks
 * on this toolchain: the real failure is a corrupt database file or a disk that will not read, and
 * nothing here can produce either.
 *
 * [observeAll] throws on **collection** rather than when it is called, because `AlarmRepositoryImpl`
 * builds its `alarmsFlow` in a property initialiser — a fake that threw from the function itself
 * would blow up in the constructor and never reach the code being tested.
 *
 * @author Phong-Kaster
 */
private class BrokenAlarmDao : AlarmDao {

    override fun observeAll(): Flow<List<AlarmEntity>> = flow { throw IOException("disk is gone") }

    override suspend fun getById(id: Long): AlarmEntity? = throw IOException("disk is gone")

    override suspend fun upsert(alarm: AlarmEntity): Long = throw IOException("disk is gone")

    override suspend fun delete(alarm: AlarmEntity): Int = throw IOException("disk is gone")
}

/**
 * The alarm behind a read the test expects to succeed.
 *
 * `AlarmRepository.getAlarm` answers with an [Outcome] rather than a nullable alarm, because "it is
 * gone" and "I could not look" are different answers. Several tests above are about *saving*
 * something they first read back, and unwrapping that in-line each time would bury the assertion
 * under ceremony.
 *
 * It asserts on the way through on purpose: a read that failed, or that found nothing, fails the
 * test here — at the read — rather than three lines later as a mystifying null.
 *
 * @param id the row id to read.
 * @author Phong-Kaster
 */
private suspend fun AlarmRepositoryImpl.storedAlarm(id: Long): Alarm {
    val outcome = getAlarm(id = id)
    assertTrue("reading alarm $id should have succeeded", outcome is Outcome.Success)

    val alarm = (outcome as Outcome.Success).data
    assertNotNull("alarm $id should be in the store", alarm)
    return alarm!!
}
