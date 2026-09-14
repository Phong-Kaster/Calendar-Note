package com.example.skeleton.data.repository

import com.example.skeleton.common.Outcome
import com.example.skeleton.data.database.local.dao.AlarmDao
import com.example.skeleton.data.database.local.entity.AlarmEntity
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mapper.toEntity
import com.example.skeleton.data.repository.impl.AlarmRepositoryImpl
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
 * a read has three answers rather than two, and **nothing that goes wrong inside ever leaves as an
 * exception**.
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

        val alarms = AlarmRepositoryImpl(alarmDao = dao).alarmsFlow.first()

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

        val alarms = AlarmRepositoryImpl(alarmDao = dao).alarmsFlow.first()

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

        val alarms = AlarmRepositoryImpl(alarmDao = dao).alarmsFlow.first()

        assertEquals(listOf(7L, 8L, 9L), alarms.map { alarm -> alarm.id })
    }

    @Test
    fun `an empty table produces an empty list, not a failure`() = runTest {
        val alarms = AlarmRepositoryImpl(alarmDao = FakeAlarmDao(rows = emptyList()))
            .alarmsFlow
            .first()

        assertEquals(emptyList<Alarm>(), alarms)
    }

    @Test
    fun `a database that will not be read shows an empty list instead of crashing the screen`() =
        runTest {
            // `alarmsFlow` is collected by a ViewModel that catches nothing, so an exception escaping
            // here would take the app down rather than showing a wrong-but-survivable empty list.
            val alarms = AlarmRepositoryImpl(alarmDao = BrokenAlarmDao(), clock = CLOCK)
                .alarmsFlow
                .first()

            assertEquals(emptyList<Alarm>(), alarms)
        }

    @Test
    fun `a stored alarm survives the trip out of the database`() = runTest {
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L, hourOfDay = 21, minute = 30)))

        val alarm = AlarmRepositoryImpl(alarmDao = dao).alarmsFlow.first().single()

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
        val repository = AlarmRepositoryImpl(alarmDao = dao, clock = CLOCK)

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
        val repository = AlarmRepositoryImpl(alarmDao = dao, clock = CLOCK)

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
        val repository = AlarmRepositoryImpl(alarmDao = dao, clock = CLOCK)

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
        val repository = AlarmRepositoryImpl(alarmDao = dao, clock = CLOCK)

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
        val repository = AlarmRepositoryImpl(alarmDao = FakeAlarmDao(rows = emptyList()), clock = CLOCK)

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
        val repository = AlarmRepositoryImpl(alarmDao = FakeAlarmDao(rows = emptyList()), clock = CLOCK)

        val outcome = repository.save(alarm = Alarm.draft(message = ""))

        assertTrue((outcome as Outcome.Error).throwable is BlankAlarmMessageException)
    }

    @Test
    fun `a write the database refuses is not dressed up as a refusal`() = runTest {
        // The other half of the line above, and the half with teeth: tagging *every* error as a
        // refusal would pass the test before this one and would tell a user whose disk is failing
        // that they forgot to write anything. The message here is perfectly good, so the rule has no
        // business firing at all.
        val repository = AlarmRepositoryImpl(alarmDao = BrokenAlarmDao(), clock = CLOCK)

        val outcome = repository.save(alarm = Alarm.draft(message = "Leave for the dentist"))

        assertTrue(outcome is Outcome.Error)
        assertFalse((outcome as Outcome.Error).throwable is BlankAlarmMessageException)
    }

    @Test
    fun `an existing alarm cannot be edited into saying nothing either`() = runTest {
        // The rule covers every write path, not just creation. Clearing the message of a stored
        // alarm is the same violation as creating one blank, and the stored row must survive it.
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L)))
        val repository = AlarmRepositoryImpl(alarmDao = dao, clock = CLOCK)

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
            val repository = AlarmRepositoryImpl(alarmDao = FakeAlarmDao(rows = emptyList()), clock = CLOCK)

            val outcome = repository.getAlarm(id = 404L)

            assertTrue(outcome is Outcome.Success)
            assertNull((outcome as Outcome.Success).data)
        }

    @Test
    fun `asking for an alarm that is there answers with it`() = runTest {
        val dao = FakeAlarmDao(rows = listOf(entity(id = 5L, hourOfDay = 21, minute = 30)))
        val repository = AlarmRepositoryImpl(alarmDao = dao, clock = CLOCK)

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
        val repository = AlarmRepositoryImpl(alarmDao = BrokenAlarmDao(), clock = CLOCK)

        val outcome = repository.getAlarm(id = 5L)

        assertTrue(outcome is Outcome.Error)
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
 * One thing it does *not* emulate: Room's `autoGenerate`. [upsert] keeps whatever id it is given, so
 * two brand-new alarms — both carrying [Alarm.UNSAVED_ID] — would land on the same row here where
 * the real table would hand out two. No test above saves two new alarms; one that needs to will need
 * a fake that counts.
 *
 * @param rows the rows the fake table starts with.
 * @author Phong-Kaster
 */
private class FakeAlarmDao(rows: List<AlarmEntity>) : AlarmDao {

    private val storedRows = MutableStateFlow(rows)

    override fun observeAll(): Flow<List<AlarmEntity>> = storedRows

    override suspend fun getById(id: Long): AlarmEntity? =
        storedRows.value.firstOrNull { row -> row.id == id }

    override suspend fun upsert(alarm: AlarmEntity): Long {
        storedRows.value = storedRows.value.filterNot { row -> row.id == alarm.id } + alarm
        return alarm.id
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
