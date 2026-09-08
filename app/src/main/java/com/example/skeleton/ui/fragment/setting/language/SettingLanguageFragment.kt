package com.example.skeleton.ui.fragment.setting.language

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.common.Language
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.ui.component.CoreTopBar4
import com.example.skeleton.ui.fragment.setting.SettingUiState
import com.example.skeleton.ui.fragment.setting.SettingViewModel
import com.example.skeleton.ui.fragment.setting.language.component.LanguageItem
import com.example.skeleton.ui.theme.customizedTextStyle
import com.example.skeleton.ui.util.LocaleManager
import com.example.skeleton.ui.util.NavigationUtil.safeNavigateUp
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class SettingLanguageFragment : CoreFragment() {


    private val viewModel: SettingViewModel by viewModel()

    private val localeManager: LocaleManager by inject()

    @Composable
    override fun ComposeView() {
        super.ComposeView()
        val uiState = viewModel.uiState.collectAsState().value
        SettingLanguageLayout(
            uiState = uiState,
            onBack = { safeNavigateUp() },
            onChangeLanguage = { language -> viewModel.changeLanguage(language = language) },
            onConfirm = {
                viewModel.setLanguage()
                localeManager.applyLocale(languageCode = uiState.selectedLanguage.code)
                safeNavigateUp()
            },
        )

    }
}

@Composable
private fun SettingLanguageLayout(
    uiState: SettingUiState,
    onBack: () -> Unit = {},
    onConfirm: () -> Unit = {},
    onChangeLanguage: (Language) -> Unit = {},
) {
    CoreLayout(
        topBar = {
            CoreTopBar4(
                title = stringResource(R.string.languages),
                backIcon = R.drawable.ic_back_2,
                onBack = onBack,
                actionContent = {
                    Text(
                        text = "Confirm",
                        style = customizedTextStyle(
                            fontSize = 14,
                            fontWeight = 600,
                            color = Color.Black,
                        ),
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(shape = RoundedCornerShape(12.dp))
                            .clickable { onConfirm() }
                            .padding(
                                vertical = 4.dp,
                                horizontal = 8.dp,
                            )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .statusBarsPadding()
            )
        },
        content = {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(
                    items = Language.entries,
                    key = { it.name },
                    itemContent = {
                        LanguageItem(
                            language = it,
                            selected = uiState.selectedLanguage == it,
                            onClick = { onChangeLanguage(it) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                )
            }
        }
    )
}

@Preview
@Composable
private fun SettingLanguageLayoutPreview() {
    SettingLanguageLayout(
        uiState = SettingUiState()
    )
}