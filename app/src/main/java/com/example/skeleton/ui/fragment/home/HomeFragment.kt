package com.example.skeleton.ui.fragment.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Post
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.home.component.HomeRequestPermission
import com.example.skeleton.ui.fragment.home.component.isNotificationGranted
import com.example.skeleton.ui.util.PermissionUtil.isLocationGranted
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : CoreFragment() {
    private val viewModel: HomeViewModel by viewModel()

    // Enable request permission
    var triggerRequestPermission by mutableIntStateOf(0)


    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    private fun checkPermission() {

        val notiEnable = isNotificationGranted(requireContext())
        val locationEnable = isLocationGranted(requireContext())

        if (!notiEnable) {
            triggerRequestPermission++
        }

        if (!locationEnable) {
            triggerRequestPermission++
        }
    }

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        HomeLayout(
            uiState = uiState,
            onRefresh = {
                viewModel.refreshPosts()
            },
        )

        // Request notification, location and exact alarm permissions
        HomeRequestPermission(
            enable = triggerRequestPermission,
            onNotificationGranted = {
                // Handle notification granted (e.g. refresh UI)
            },
            onLocationGranted = {
                // Handle location granted (e.g. refresh location-based data)
            },
            onExactAlarmGranted = {
                // Handle exact alarm granted (e.g. reschedule alarms)
            }
        )
    }
}

/**
 * Home screen: shows posts from [HomeUiState], refresh action, loading strip, and API error text.
 */
@Composable
private fun HomeLayout(
    uiState: HomeUiState,
    onRefresh: () -> Unit = {},
) {
    CoreLayout(
        modifier = Modifier,
        topBar = { CoreTopBar(title = stringResource(R.string.home)) },
        bottomBar = { CoreBottomBar() },
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    if (uiState.isRefreshing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                item {
                    uiState.refreshError?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                item {
                    Button(
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(R.string.home_refresh))
                    }
                }
                if (uiState.posts.isEmpty() && !uiState.isRefreshing) {
                    item {
                        Text(
                            text = stringResource(R.string.home_no_posts),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                items(
                    items = uiState.posts,
                    key = { post -> post.id },
                ) { post ->
                    PostListItem(post = post)
                }
            }
        }
    )
}

/**
 * One row for a [Post]: title and a short body preview.
 */
@Composable
private fun PostListItem(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun HomeLayoutPreview() {
    HomeLayout(
        uiState = HomeUiState(
            posts = listOf(
                Post(
                    id = 1,
                    userId = 1,
                    title = "Sample title",
                    body = "Sample body text for preview.",
                ),
            ),
        ),
    )
}