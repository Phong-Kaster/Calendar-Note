package com.example.skeleton.ui.fragment.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.calendar.component.CalendarMonthGrid
import com.example.skeleton.ui.fragment.calendar.component.CalendarMonthHeader
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Calendar screen: shows the current month and lets the user step to the previous/next month.
 *
 * @author Phong-Kaster
 */
class CalendarFragment : CoreFragment() {
    private val viewModel: CalendarViewModel by viewModel()

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        CalendarLayout(
            uiState = uiState,
            onPrevious = { viewModel.previousMonth() },
            onNext = { viewModel.nextMonth() },
        )
    }
}

/**
 * Calendar screen: month header with prev/next navigation above a 7-column day grid.
 */
@Composable
private fun CalendarLayout(
    uiState: CalendarUiState,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    val monthLabel = uiState.currentMonth.yearMonth.format(
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    )

    CoreLayout(
        modifier = Modifier,
        topBar = { CoreTopBar(title = stringResource(R.string.calendar)) },
        bottomBar = { CoreBottomBar() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                CalendarMonthHeader(
                    label = monthLabel,
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
                CalendarMonthGrid(
                    days = uiState.currentMonth.buildGrid(),
                    today = uiState.today,
                )
            }
        }
    )
}

@Preview
@Composable
private fun CalendarLayoutPreview() {
    CalendarLayout(uiState = CalendarUiState())
}
