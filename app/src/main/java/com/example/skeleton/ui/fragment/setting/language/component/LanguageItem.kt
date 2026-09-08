package com.example.skeleton.ui.fragment.setting.language.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.common.Language
import com.example.skeleton.ui.theme.customizedTextStyle

@Composable
fun LanguageItem(
    language: Language,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(57.dp)
            .clip(shape = RoundedCornerShape(16.dp))
            .then(
                if (selected) {
                    Modifier
                        .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                        .background(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        )
                } else {
                    Modifier.background(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(0.1f)
                    )
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = {
            Image(
                painter = painterResource(language.drawable),
                contentDescription = language.name,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit,
            )

            Text(
                text = stringResource(language.displayText),
                style = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = 600,
                    color = Color.White,
                ),
                modifier = Modifier.weight(1f),
            )

            Icon(
                painter =
                    if (selected)
                        painterResource(R.drawable.ic_radio_active)
                    else
                        painterResource(R.drawable.ic_radio_inactive),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    )
}

@Preview
@Composable
private fun PreviewLanguageItem() {
    Column(modifier = Modifier) {
        LanguageItem(
            language = Language.English,
            selected = true,
            onClick = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        LanguageItem(
            language = Language.English,
            selected = false,
            onClick = {}
        )
    }

}
