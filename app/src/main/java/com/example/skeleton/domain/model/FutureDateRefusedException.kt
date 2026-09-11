package com.example.skeleton.domain.model

import java.time.LocalDate

/*
 * --- Why an exception that is never thrown (simple story) ---
 *
 * `NoteRepository.save` answers with `common.Outcome`, and a refused save and a broken disk both
 * come back as `Outcome.Error`. To the screen above, those two are not the same event at all:
 *
 *   - a **write that failed** is worth retrying — the disk may be there next time;
 *   - a **refusal** can never succeed on a retry, because nothing about the note has changed.
 *
 * Told apart only by their message text, the screen showed one sentence for both ("something went
 * wrong, please try again") and so invited the user to keep tapping Save at a note that will be
 * refused every time. Matching on message strings instead would be worse: a wording change in one
 * file would silently break the branch in another.
 *
 * `Outcome.Error` carries a `throwable`, which is the one *typed* channel it has — so the refusal
 * travels in it as a value. It is constructed and handed over, never thrown: `knowledge/DOMAIN.md`
 * says the refusal is reported as a value, and `.claude/repository-layer.md` says a repository does
 * not throw across its boundary. It is an `Exception` subclass because that is the type the slot
 * takes, and for no other reason.
 */

/**
 * The note store's calendar rule saying no: this note is dated after today.
 *
 * Lives in an `Outcome.Error`'s `throwable` so a caller can tell "I will not store this" from
 * "I could not store this". See the note above for why it is an exception that nobody throws.
 *
 * @param date the day the note was dated to — the day to name when telling the user.
 * @param today the day the store compared it against, read from the store's own clock. Kept
 *   alongside [date] so a log line can show both; a refusal where the two look adjacent is a clock
 *   problem, and one where they are months apart is a caller problem.
 * @author Phong-Kaster
 */
class FutureDateRefusedException(
    val date: LocalDate,
    val today: LocalDate,
) : Exception(
    /* message = */ "A note cannot be dated $date, which is after $today.",
    /* cause = */ null,
    /* enableSuppression = */ false,
    // No stack trace. Capturing one costs a walk up the stack on every refusal and records where
    // this was *constructed*, which nobody will ever read: it is not thrown, so there is no
    // throw site to trace back to. The two dates above are the whole of what a reader wants.
    /* writableStackTrace = */ false,
)
