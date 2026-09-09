package com.example.skeleton.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.skeleton.R
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * A yes/no confirmation dialog for actions that destroy something.
 *
 * Renders nothing while [visible] is false, matching the convention the edit dialogs already use
 * (state lives in the screen's UiState; the dialog is a pure function of it).
 *
 * The confirm action is tinted with `error` rather than `primary`: the whole point of the dialog
 * is that the two buttons should not look equally safe to tap.
 *
 * @param visible whether the dialog is shown.
 * @param title the question, e.g. "Delete this task?".
 * @param confirmLabel label of the destructive button.
 * @param onConfirm called when the user confirms; the caller both performs the action and clears
 * the state that made [visible] true.
 * @param onDismiss called on cancel, back press, or an outside tap.
 * @author Phong-Kaster
 */
@Composable
fun CoreConfirmDialog(
    visible: Boolean,
    title: String,
    confirmLabel: String = stringResource(R.string.delete),
    body: String = stringResource(R.string.delete_confirm_body),
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = customizedTextStyle(
                    fontSize = 18,
                    fontWeight = 600,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        text = {
            Text(
                text = body,
                style = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = 400,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    style = customizedTextStyle(
                        fontSize = 14,
                        fontWeight = 600,
                        color = MaterialTheme.colorScheme.error,
                    ),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = customizedTextStyle(
                        fontSize = 14,
                        fontWeight = 400,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Preview
@Composable
private fun CoreConfirmDialogPreview() {
    CoreConfirmDialog(
        visible = true,
        title = "Delete this task?",
    )
}
