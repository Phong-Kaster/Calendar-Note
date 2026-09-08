package com.example.skeleton.ui.fragment.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skeleton.R

/**
 * Setting item component matching KMP design.
 */
@Composable
fun SettingItem(
    showDivider: Boolean = true,
    modifier: Modifier = Modifier,
    nameRes: Int,
    iconRes: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 24.dp)
                .background(color = Color.Transparent)
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = iconRes),
                contentDescription = "",
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = stringResource(nameRes),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                ),
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(id = R.drawable.ic_navigate),
                contentDescription = "",
                tint = Color.White
            )
        }

        if (showDivider) {
            HorizontalDivider(
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun SettingItemPreview() {
    SettingItem(
        nameRes = R.string.setting,
        iconRes = R.drawable.ic_language_setting,
        onClick = {}
    )
}