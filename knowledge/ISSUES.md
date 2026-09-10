# OPEN ISSUES

> Engine-maintained list of **known-wrong things that are still wrong**. Survives every feature run
> because it lives in `knowledge/`, alongside `PROJECT.md`, and is read at Orient every iteration.
>
> **Its semantics are the opposite of `PROJECT.md`'s.** `PROJECT.md` records how this repository
> *is* — conventions to conform to. This file records what is *wrong* with it — things to **not**
> copy.
>
> Human-editable without approval, like `PROJECT.md`.
>
> Entries are self-contained and cite the **commit SHA** holding the full record, never a path into
> `.ai/` alone (the Cleanup Commit removes `.ai/`). Read a record back with `git show <sha>:<path>`.

## Entries

<!-- Newest first. Delete resolved entries outright rather than marking them done. -->

### `CLAUDE.md` imports three rule files that do not exist

- **What is wrong:** `CLAUDE.md` `@`-imports `.claude/view-model-layer.md`,
  `.claude/jetpack-compose-ui-layer.md` and `.claude/wiki-connection.md`. None of those files exist.
  The real files are `.claude/viewmodel-layer.md` (no hyphen) and `.claude/jetpack-compose-ui.md`
  (no `-layer` suffix); there is no wiki rule file at all. `.claude/usecase-layer.md` and
  `.claude/figma-design-system.md` exist and are referenced only indirectly.
- **Where:** `CLAUDE.md:1-5`
- **Why it is still open:** `CLAUDE.md` and `.claude/*.md` are human-owned rule sources. ENGINE.md §5
  says these are inspected, never edited. Fixing the import names is the human's call.
- **What would resolve it:** the human renames the imports to the real filenames (or renames the
  files), and decides whether `wiki-connection.md` should exist.
- **Full record:** `git show 9b25307:knowledge/PROJECT.md` — the *Sources Consulted* section lists
  which rule files were actually read.
- **Do not:** assume the ViewModel and Compose rules are unavailable because the import is broken.
  They were read directly from `.claude/viewmodel-layer.md` and `.claude/jetpack-compose-ui.md` at
  bootstrap and their conventions are summarised in `knowledge/PROJECT.md`. Also do not "fix" this by
  creating the missing files — that invents human intent.

### No toolchain command has ever been executed in this repository

- **What is wrong:** every build/test/lint command in `knowledge/PROJECT.md` is marked
  `Verified: no`. At bootstrap `./gradlew --version` was refused by the permission layer, so the
  project has never been proven to compile under this engine, and the recorded task names are
  inferred from the AGP defaults rather than observed.
- **Where:** `knowledge/PROJECT.md` § Toolchain
- **Why it is still open:** the capability is now **granted** (escalation D-001 →
  `knowledge/capabilities.json`), but it was granted after iteration 1's permissions had already been
  compiled, so nothing has run yet. The Runtime recompiles permissions each iteration, so the next
  invocation is the first that can.
- **What would resolve it:** the next iteration runs the build and replaces the `no` entries in
  `knowledge/PROJECT.md` with real observed output, then deletes this entry. **Treat "does this
  project build at all?" as that iteration's first finding** — a skeleton that has never been
  compiled by anyone in this run is an assumption, not a fact.
- **Full record:** `git show 9b25307:.ai/ESCALATION.md` — the capability proposals (C1, C2, C3).
- **Do not:** mark any task complete on the strength of "the code looks right". ENGINE.md §10
  requires build/test/lint evidence, and until this entry is gone there is none.
