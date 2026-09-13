package com.example.skeleton.domain.model

/*
 * --- Why an exception that is never thrown (simple story) ---
 *
 * `AlarmRepository.save` answers with `common.Outcome`, and a refused save and a broken disk both
 * come back as `Outcome.Error`. To the screen above, those two are not the same event at all:
 *
 *   - a **write that failed** is worth retrying — the disk may be there next time;
 *   - a **refusal** can never succeed on a retry, because nothing about the alarm has changed.
 *
 * Told apart only by their message text, the screen would show one sentence for both ("something
 * went wrong, please try again") and so invite the user to keep tapping Save at an alarm that will
 * be refused every time. Matching on message strings instead would be worse: a wording change in
 * one file would silently break the branch in another.
 *
 * `Outcome.Error` carries a `throwable`, which is the one *typed* channel it has — so the refusal
 * travels in it as a value. It is constructed and handed over, never thrown:
 * `.claude/repository-layer.md` says a repository does not throw across its boundary. It is an
 * `Exception` subclass because that is the type the slot takes, and for no other reason.
 *
 * This is the same pattern as `FutureDateRefusedException`, deliberately. Two refusals that look
 * different to a caller would be two refusals somebody has to learn separately.
 */

/**
 * The alarm store saying no: this alarm has nothing written on it.
 *
 * Lives in an `Outcome.Error`'s `throwable` so a caller can tell "I will not store this" from "I
 * could not store this". See the note above for why it is an exception that nobody throws.
 *
 * It carries no fields, unlike [FutureDateRefusedException], because there is nothing to name: the
 * whole of the refusal is "the message was blank", and the message the user reads has nothing to
 * fill in.
 *
 * @author Phong-Kaster
 */
class BlankAlarmMessageException : Exception(
    /* message = */ "An alarm cannot be saved with a blank message.",
    /* cause = */ null,
    /* enableSuppression = */ false,
    // No stack trace. Capturing one costs a walk up the stack on every refusal and records where
    // this was *constructed*, which nobody will ever read: it is not thrown, so there is no throw
    // site to trace back to.
    /* writableStackTrace = */ false,
)
