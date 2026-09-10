package com.example.skeleton.ui.fragment.note.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * The writing surface of the Note screen: which day this note belongs to, its heading, and its
 * body.
 *
 * **It scrolls, and that is the point of the outer `Column`.** The body has no upper length — a
 * user can type until they stop — and a body taller than the screen inside a plain `Column` is
 * simply cut off, with the end of their own sentence somewhere below the bottom edge and no way to
 * reach it. Note that the *save* control is deliberately not in here: it lives in the screen's top
 * bar, so scrolling to the end of a long note is never a prerequisite for keeping it.
 *
 * **`imePadding()` is what makes that scrolling true while the keyboard is up**, which is the only
 * time it matters on a screen whose whole job is typing. This app is edge-to-edge
 * (`MainActivity.enableEdgeToEdge()`, and `CoreLayout` zeroes its own `contentWindowInsets`), so
 * the window is *not* resized when the keyboard appears — without this line the viewport stays
 * full-screen height, the keyboard covers the bottom of it, and the scroll range can never lift
 * that covered strip into view. The user types into a line they cannot see. It goes before
 * `verticalScroll` on purpose: it has to shrink the viewport, not the content inside it.
 *
 * Both fields are `BasicTextField` rather than Material's `TextField`, matching the rest of this
 * app: Material's version brings its own container, label and indicator line, all coloured from
 * roles this dark theme would then have to fight.
 *
 * @param date the day the note belongs to; drawn, never edited here.
 * @param title the current heading.
 * @param content the current body.
 * @param onTitleChange called on every keystroke in the heading.
 * @param onContentChange called on every keystroke in the body.
 * @param modifier applied to the scrolling container.
 * @author Phong-Kaster
 */
@Composable
fun NoteEditor(
    date: LocalDate,
    title: String,
    content: String,
    onTitleChange: (String) -> Unit = {},
    onContentChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // The date is formatted in the language the *app* is showing, which is not always the language
    // of the device: this app has its own picker in Settings. `Locale.getDefault()` answers for the
    // device, so a user who switched the app to German could read an English date under German
    // copy. Compose's configuration is the one that follows the picker.
    val locale = LocalConfiguration.current.locales[0]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = date.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale),
            ),
            style = customizedTextStyle(
                fontSize = 12,
                fontWeight = 500,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        NoteTextField(
            value = title,
            hint = stringResource(R.string.title),
            onValueChange = onTitleChange,
            textStyle = customizedTextStyle(fontSize = 20, fontWeight = 600),
            singleLine = true,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        NoteTextField(
            value = content,
            hint = stringResource(R.string.write_your_note_here),
            onValueChange = onContentChange,
            textStyle = customizedTextStyle(fontSize = 16, fontWeight = 400),
            singleLine = false,
            // A comfortable target rather than a real constraint: with nothing typed yet the body
            // would otherwise be one line tall, and the large empty area under it — the part that
            // looks like the page — would not respond to a tap.
            modifier = Modifier.heightIn(min = 240.dp),
        )
    }
}

/**
 * One field of the editor, so the heading and the body cannot drift apart in look or behaviour.
 *
 * @param value the text to show.
 * @param hint what to draw while [value] is empty. Without it an empty field is an unlabelled
 *   blank area, and nobody can tell which one is the title.
 * @param textStyle how the text itself is drawn — the one thing that genuinely differs between the
 *   heading and the body.
 * @param singleLine true for the heading: a title that wraps onto three lines is a body.
 * @param onValueChange called on every keystroke.
 * @param modifier applied to the field.
 * @author Phong-Kaster
 */
@Composable
private fun NoteTextField(
    value: String,
    hint: String,
    textStyle: TextStyle,
    singleLine: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = textStyle,
        singleLine = singleLine,
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            // The heading's Enter key moves to the body instead of inserting a newline a
            // single-line field cannot show anyway.
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
        ),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = hint,
                    style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
            innerTextField()
        },
    )
}

@Preview(name = "Note editor - empty", widthDp = 360, heightDp = 420)
@Composable
private fun NoteEditorEmptyPreview() {
    MyApplicationTheme {
        NoteEditor(
            date = LocalDate.of(2026, 3, 14),
            title = "",
            content = "",
        )
    }
}

@Preview(name = "Note editor - written", widthDp = 360, heightDp = 420)
@Composable
private fun NoteEditorWrittenPreview() {
    MyApplicationTheme {
        NoteEditor(
            date = LocalDate.of(2026, 3, 14),
            title = "Groceries",
            content = "Coffee, oat milk, the good bread from the corner shop.",
        )
    }
}
