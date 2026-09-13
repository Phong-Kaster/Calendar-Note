package com.example.skeleton.ui.fragment.alarms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.alarms.component.AlarmsEmptyState
import com.example.skeleton.ui.theme.MyApplicationTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The Alarms screen: the fourth top-level tab.
 *
 * It shows nothing yet, and it says so. No alarm can be stored at this point — there is no model,
 * no store and no way to make one — so the screen's only job for now is to exist, be reachable from
 * the bottom bar, and print an empty message rather than a blank rectangle.
 *
 * Thin, like every Fragment here: it owns the ViewModel and hands the drawing to [AlarmsLayout].
 * It navigates nowhere at all, which is why it has no navigation lambdas — the "add an alarm"
 * route arrives with the task that can actually save one.
 *
 * @author Phong-Kaster
 */
class AlarmsFragment : CoreFragment() {
    private val viewModel: AlarmsViewModel by viewModel()

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        AlarmsLayout(uiState = uiState)
    }
}

/**
 * The Alarms screen, drawn: a title, the bottom bar, and — while there are no alarms — a line
 * saying there are none.
 *
 * Pure UI. It takes state and nothing else, which is what lets the previews below render the whole
 * screen with no ViewModel, no Koin and no database behind them.
 *
 * **`onCreateNote = {}` is a deliberate no-op and not an oversight.** `CoreBottomBar` refuses a
 * default for that lambda precisely so nobody ships the app's create action as a dead button — but
 * on this destination the centre "+" is not drawn at all (the bar hides it for Alarms), so there is
 * no button here for the empty lambda to disappoint. This screen's own add action comes later, with
 * the code that can store what it creates.
 *
 * @param uiState what to draw.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmsLayout(
    uiState: AlarmsUiState,
) {
    CoreLayout(
        modifier = Modifier,
        topBar = { CoreTopBar(title = stringResource(R.string.alarms)) },
        bottomBar = { CoreBottomBar(onCreateNote = {}) },
        content = {
            if (uiState.isEmpty) {
                AlarmsEmptyState()
            }
        },
    )
}

@Preview(name = "Alarms - empty", widthDp = 360, heightDp = 780)
@Composable
private fun AlarmsLayoutEmptyPreview() {
    // Sized as the real full screen rather than as the content area. A preview smaller than the
    // thing it renders does not record as clipped — it records as empty, so a too-small frame here
    // would photograph a working empty state and a broken one identically.
    MyApplicationTheme(
        content = {
            AlarmsLayout(uiState = AlarmsUiState())
        },
    )
}
