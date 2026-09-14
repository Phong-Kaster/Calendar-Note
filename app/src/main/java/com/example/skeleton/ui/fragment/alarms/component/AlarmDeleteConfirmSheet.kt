package com.example.skeleton.ui.fragment.alarms.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.component.CoreBottomSheet
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * The step between "delete" and the alarm actually going away.
 *
 * Deleting is the only thing in this app that cannot be undone, so it does not happen on one tap.
 * The bin on an alarm row opens this; only the button inside it reaches the store — and that is a
 * fact about `AlarmsViewModel`, not a habit of this sheet: `confirmDelete()` does nothing at all
 * unless an alarm is waiting on a confirmation.
 *
 * It goes through [CoreBottomSheet] rather than a raw `ModalBottomSheet` because that wrapper
 * already handles the navigation-bar inset, the hide animation and the dark container colour — the
 * three things a hand-rolled sheet in this project gets wrong.
 *
 * @param enable whether the sheet is on screen. Forwarded to the wrapper, which decides what to do
 *   with it — this composable does not return early on it.
 * @param onCancel the user backed out. Nothing is deleted.
 * @param onConfirm the user confirmed. **This is the only route to the store.**
 * @author Phong-Kaster
 */
// `CoreBottomSheet` defaults its `sheetState` to `rememberModalBottomSheetState(...)`, which drags
// the experimental Material 3 sheet API into every call site. Every other sheet in this project opts
// in the same way, on the calling function.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDeleteConfirmSheet(
    enable: Boolean,
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    CoreBottomSheet(
        enable = enable,
        onDismissRequest = onCancel,
        content = {
            AlarmDeleteConfirmContent(
                onCancel = onCancel,
                onConfirm = onConfirm,
            )
        },
    )
}

/**
 * What the confirmation actually says, and the two controls under it.
 *
 * **The two controls are deliberately not siblings in look.** "Visually distinct" here means two
 * things at once, not one:
 *
 * - a **different colour role** — the destructive button is filled with `error`; the way out is not
 *   filled at all;
 * - a **different emphasis** — one is a solid block of colour with bold text, the other is plain
 *   text at normal weight.
 *
 * Distinguishing them by wording alone is what makes a user tap "Delete" while reading "Cancel",
 * and a colour difference on two otherwise identical buttons is nearly as bad on a dark screen.
 * Cancel is on the left and Delete on the right, following the platform convention, so muscle
 * memory is not fighting the layout either.
 *
 * Split out from the sheet above so it can be rendered on its own, which is what a screenshot case
 * in `app/src/screenshotTest/` does: a picture of these two controls is the only evidence the
 * "visually distinct" clause can ever have, since no command can look at a screen. It renders
 * outside a `ModalBottomSheet` there, and that is the reason for the split — a modal sheet draws
 * into its own window, which is not something this project has established with layoutlib.
 *
 * @param onCancel the user backed out.
 * @param onConfirm the user confirmed the deletion.
 * @param modifier applied to the column.
 * @author Phong-Kaster
 */
@Composable
fun AlarmDeleteConfirmContent(
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.delete_this_alarm),
            style = customizedTextStyle(
                fontSize = 18,
                fontWeight = 600,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )

        Text(
            text = stringResource(R.string.the_alarm_will_be_removed_permanently),
            style = customizedTextStyle(
                fontSize = 14,
                fontWeight = 400,
                lineHeight = 20,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.End),
        ) {
            AlarmDeleteConfirmAction(
                label = stringResource(R.string.cancel),
                textStyle = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = 400,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                onClick = onCancel,
            )

            AlarmDeleteConfirmAction(
                label = stringResource(R.string.delete),
                textStyle = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = 600,
                    color = MaterialTheme.colorScheme.onError,
                ),
                background = MaterialTheme.colorScheme.error,
                onClick = onConfirm,
            )
        }
    }
}

/**
 * One of the confirmation's two controls.
 *
 * Both go through the same composable so the pair can only differ in the ways this function takes
 * arguments for — the fill and the text style. Two hand-written buttons drift apart in padding and
 * corner radius, and then the difference between them stops reading as *meaning* and starts reading
 * as sloppiness.
 *
 * The vertical padding is 14dp rather than something smaller so that the label plus its padding
 * clears the 48dp minimum touch target. That matters most for Cancel: the way out of a destructive
 * action must not be the harder of the two to hit.
 *
 * @param label the words on the control.
 * @param textStyle how those words are drawn — this is half of what tells the two apart.
 * @param onClick the user tapped it.
 * @param background the fill behind the label. Transparent by default, which is the un-emphasised
 *   control; hand it a colour and it becomes the emphasised one.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmDeleteConfirmAction(
    label: String,
    textStyle: TextStyle,
    onClick: () -> Unit,
    background: Color = Color.Transparent,
) {
    Text(
        text = label,
        style = textStyle,
        modifier = Modifier
            // Clipped first so the fill and the ripple both stop at the rounded corners.
            .clip(shape = RoundedCornerShape(12.dp))
            .background(color = background)
            .clickable(
                onClickLabel = label,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            // After the click, so the padding is inside the tap target.
            .padding(vertical = 14.dp, horizontal = 20.dp),
    )
}

@Preview(name = "Alarm delete confirmation", widthDp = 360, heightDp = 240)
@Composable
private fun AlarmDeleteConfirmContentPreview() {
    MyApplicationTheme {
        AlarmDeleteConfirmContent()
    }
}
