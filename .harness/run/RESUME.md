# RESUME BLOCK

> Regenerated every iteration. The first thing a fresh iteration reads, and usually the only thing
> it needs before selecting work.
>
> **A derived cache, never a source of truth.** On any disagreement with the task files or with git,
> this file is the one that is wrong: correct it and trust the source. Recovery from a dirty tree
> always reads ground truth, never this.

- **Stage:** in-progress
- **Next Phase:** Phase 2 (T-003, T-006) — both dependencies (T-002, T-005) now complete
  - T-003: `domain/repository/TaskRepository.kt`, `data/database/local/dao/TaskDao.kt`, `data/repository/impl/TaskRepositoryImpl.kt`, `ui/fragment/todo/TodoViewModel.kt`, `ui/fragment/todo/component/TodoTaskItem.kt`, `app/src/test/.../todo/{FakeTaskRepository,TaskToggleAndDeleteTest}.kt`
  - T-006: `domain/model/Note.kt`, `domain/repository/NoteRepository.kt`, `data/database/local/entity/NoteEntity.kt`, `data/database/local/dao/NoteDao.kt`, `data/mapper/NoteMapper.kt`, `data/repository/impl/NoteRepositoryImpl.kt`, `ui/fragment/calendar/{CalendarUiState,CalendarViewModel,CalendarFragment}.kt`, `ui/fragment/calendar/component/{CalendarDayCell,CalendarNoteList,CalendarAddNoteRow}.kt`, `app/src/test/.../calendar/{FakeNoteRepository,NoteDateScopingTest}.kt`
  - Shared files the Iteration wires after Phase 2 (per PLAN.md): `AppDatabase.kt` (register `NoteEntity`/`NoteDao`, version → 4), `Migration.kt` (`MIGRATION_3_4`), `DatabaseModule.kt`, `RepositoryModule.kt`, `ViewModelModule.kt` (`CalendarViewModel` gains `noteRepository`), `strings.xml`/`values-de/strings.xml`
  - Phase 3 next (after Phase 2 checkpoints): T-004, T-007
- **Queued decisions:** 1 — D-002 (blocks no task; blocks only final DoD sign-off on criterion 30 at
  Verification, §ENGINE 11)
- **Abandoned:** none
- **Unreachable:** none
- **Verified commands:** build: `gradlew.bat assembleDebug` (verified, passes) | test: `gradlew.bat test`
  (verified, passes — 6 new + 1 pre-existing) | lint: `gradlew.bat lint` (verified, but not green — 4
  pre-existing `MissingTranslation` errors unrelated to any task; not a DoD criterion, do not treat as a
  Phase blocker unless the error count increases)
