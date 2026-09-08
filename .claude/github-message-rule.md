# Skill: Git Commit Message Convention

## Objective

Always generate Git commit messages using the project's commit convention. Select the most appropriate commit type based on the nature of the change.

---

# Commit Format

```text
<type>(<scope>): <summary>
```

Examples:

```text
feature(scan-food): support meal history

fix(settings): prevent duplicate sign-in requests

refactor(dialog): simplify exit dialog handling
```

---

# Commit Types

## feature

Use when introducing **new functionality**.

Examples:

```text
feature(scan-food): support multiple image selection

feature(settings): add reviewer login
```

---

## fix

Use when fixing a **bug** in an existing feature.

Examples:

```text
fix(scan-food): prevent duplicate analysis requests

fix(home): correct reminder tooltip behavior
```

---

## refactor

Use when changing the internal implementation **without changing observable behavior**.

Typical cases include:

* Rename classes
* Rename functions
* Move files
* Move packages
* Split large classes
* Merge duplicated logic
* Improve architecture
* Reorganize project structure
* Simplify implementation

Do **not** use this type if the application's behavior changes.

Examples:

```text
refactor(scan-food): extract image processing pipeline

refactor(settings): simplify account state management
```

---

## docs

Use when adding or updating documentation only.

Examples include:

* README
* Architecture documents
* KDoc
* Javadoc
* Inline documentation
* Code comments

Examples:

```text
docs: add Scan Food architecture documentation

docs(settings): document authentication flow
```

---

## build

Use when changing the build system or project configuration.

Examples include:

* Dependency versions
* Gradle configuration
* Kotlin version
* Build scripts
* Build settings
* Signing configuration

Examples:

```text
build: upgrade Kotlin to 2.3.0

build: update Firebase dependencies
```

---

## test

Use when adding or updating tests.

Examples include:

* Unit tests
* Integration tests
* UI tests
* Test utilities

Examples:

```text
test(scan-food): add ViewModel unit tests

test(settings): cover login failure scenarios
```

---

## performance

Use when improving performance without changing functionality.

Examples:

```text
performance(scan-food): reduce bitmap memory usage

performance(home): improve startup rendering
```

---

## style

Use when making formatting or style-only changes.

Examples include:

* Formatting
* Import ordering
* Whitespace
* Lint fixes

No logic changes should be included.

Examples:

```text
style: apply ktlint formatting

style(settings): reorder imports
```

---

## chore

Use for project maintenance or cleanup that does not fit another commit type.

Examples include:

* Remove unused code
* Delete obsolete resources
* Update tooling
* Cleanup scripts

Examples:

```text
chore: remove deprecated utilities

chore(scan-food): delete obsolete resources
```

---

## ci

Use when changing Continuous Integration or Continuous Deployment workflows.

Examples include:

* GitHub Actions
* Bitrise
* Jenkins
* Release pipelines

Examples:

```text
ci: add release workflow

ci: update pull request validation
```

---

## revert

Use when reverting a previous commit.

Examples:

```text
revert: feature(settings): add reviewer login
```

---

# Scope Guidelines

The scope should represent the **feature or functional area**, not the implementation detail.

Good scopes:

* scan-food
* settings
* home
* dialog
* nutrition
* onboarding
* analytics
* ads
* profile
* camera

Avoid implementation-focused scopes unless the change truly targets infrastructure:

* viewmodel
* repository
* fragment
* activity
* compose
* mapper

---

# Summary Guidelines

The summary should:

* Start with an imperative verb.
* Clearly describe what changed.
* Be concise.
* Not end with a period.

Good examples:

```text
prevent duplicate scan requests

support reviewer preview mode

simplify image processing pipeline
```

Avoid:

```text
fixed duplicate scan requests

adding reviewer preview mode

miscellaneous updates
```

---

# Commit Type Selection

Choose the commit type using the following priority:

1. **feature** → New functionality
2. **fix** → Bug fix
3. **refactor** → Internal code changes with no behavior changes
4. **docs** → Documentation only
5. **build** → Build system or configuration changes
6. **test** → Add or update tests
7. **performance** → Performance improvements
8. **style** → Formatting only
9. **chore** → Maintenance or cleanup
10. **ci** → CI/CD workflow changes
11. **revert** → Revert a previous commit

Always choose the most specific commit type that accurately describes the change.