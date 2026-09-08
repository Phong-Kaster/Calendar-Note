package com.example.skeleton.ui.fragment.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.component.ratebottomsheet.RateBottomSheet
import com.example.skeleton.ui.fragment.setting.component.SettingItem
import com.example.skeleton.ui.util.AppUtil
import com.example.skeleton.ui.util.NavigationUtil.safeNavigate
import com.example.skeleton.ui.util.RateUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingFragment : CoreFragment() {
    private val viewModel: SettingViewModel by viewModel()
    private var showRateBottomSheet by mutableStateOf(false)

    @Composable
    override fun ComposeView() {
        super.ComposeView()
        SettingLayout(
            onOpenLanguage = { safeNavigate(R.id.toSettingLanguage) },
            onOpenPrivacyPolicy = {
                AppUtil.openWebsite(
                    context = requireContext(),
                    "https://www.youtube.com/"
                )
            },
            onOpenTermOfService = {
                AppUtil.openWebsite(
                    context = requireContext(),
                    "https://www.google.com/"
                )
            },
            onShareApp = { AppUtil.shareApp(context = requireContext()) },
            onRateApp = { showRateBottomSheet = true },
        )


        // Rating app
        RateBottomSheet(
            enabled = showRateBottomSheet,
            onDismiss = { showRateBottomSheet = false },
            onSubmit = { feedbackStar, feedbackMessage ->
                // users vote 4 star or 5 star -> Open Google Play Store review
                if (feedbackStar in 4..5) {
                    RateUtil.sendFeedbackToGooglePlay(activity = requireActivity())
                } else {
                    // users vote 3 star or lower -> Compose email and send to internal email address
                    RateUtil.composeEmail(
                        context = requireActivity(),
                        feedbackStar = feedbackStar,
                        feedbackMessage = feedbackMessage,
                    )
                }
            },
        )
    }
}

@Composable
private fun SettingLayout(
    onOpenLanguage: () -> Unit = {},
    onRateApp: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onOpenTermOfService: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},

    ) {
    CoreLayout(
        bottomBar = {
            CoreBottomBar()
        },
        topBar = {
            CoreTopBar(title = "Settings")
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Settings content - Top items
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .wrapContentHeight()
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Languages
                    SettingItem(
                        nameRes = R.string.languages,
                        iconRes = R.drawable.ic_language_setting,
                        onClick = onOpenLanguage,
                        showDivider = true
                    )

                    // Rate app
                    SettingItem(
                        nameRes = R.string.rate_app,
                        iconRes = R.drawable.ic_star,
                        onClick = onRateApp,
                        showDivider = true
                    )

                    // Share app
                    SettingItem(
                        nameRes = R.string.share_app,
                        iconRes = R.drawable.ic_share,
                        onClick = onShareApp,
                        showDivider = true
                    )

                    SettingItem(
                        nameRes = R.string.terms_of_service,
                        iconRes = R.drawable.ic_term_of_use,
                        onClick = onOpenTermOfService,
                        showDivider = true
                    )

                    // Settings items
                    SettingItem(
                        nameRes = R.string.privacy_policy,
                        iconRes = R.drawable.ic_privacy,
                        onClick = onOpenPrivacyPolicy,
                        showDivider = false
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun PreviewSettingLayout() {
    SettingLayout(
    )
}