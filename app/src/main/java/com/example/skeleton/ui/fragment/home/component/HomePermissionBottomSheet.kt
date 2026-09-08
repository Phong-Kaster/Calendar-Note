package com.example.skeleton.ui.fragment.home.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skeleton.R
import android.os.Build
import com.example.skeleton.ui.theme.ColorBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePermissionBottomSheet(
    enable: Boolean,
    isNotificationEnable: Boolean = false,
    isLocationEnable: Boolean = false,
    isExactAlarmEnable: Boolean = true,
    onDismiss: () -> Unit = {},
    onGrantNotification: () -> Unit = {},
    onGrantLocation: () -> Unit = {},
    onGrantExactAlarm: () -> Unit = {},
) {
    if (!enable) return
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
        dragHandle = {},
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF201E21),
        contentColor = Color.White
    ) {
        PhotosPermissionBottomSheetLayout(
            onDismiss = onDismiss,
            isNotificationEnable = isNotificationEnable,
            isLocationEnable = isLocationEnable,
            isExactAlarmEnable = isExactAlarmEnable,
            onGrantNotification = onGrantNotification,
            onGrantLocation = onGrantLocation,
            onGrantExactAlarm = onGrantExactAlarm,
        )
    }
}

@Composable
private fun PhotosPermissionBottomSheetLayout(
    onDismiss: () -> Unit = {},
    isNotificationEnable: Boolean = false,
    isLocationEnable: Boolean = false,
    isExactAlarmEnable: Boolean = true,
    onGrantNotification: () -> Unit = {},
    onGrantLocation: () -> Unit = {},
    onGrantExactAlarm: () -> Unit = {},
) {
    Column {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            // Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onDismiss() }
                )
            }

            // Lock icon placeholder (you can add a drawable resource if needed)
            Icon(
                modifier = Modifier.size(107.dp),
                painter = painterResource(R.drawable.ic_gallery),
                tint = ColorBlue,
                contentDescription = "",
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notification switch & rationale
            PermissionSwitch(
                title = stringResource(R.string.notification),
                description = stringResource(R.string.allow_notification_to_send_you),
                checked = isNotificationEnable,
                onCheckedChange = { onGrantNotification() },
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            )

            // Location switch & rationale
            PermissionSwitch(
                title = stringResource(R.string.location),
                description = stringResource(R.string.allow_location_to_help_you),
                checked = isLocationEnable,
                onCheckedChange = { onGrantLocation() },
            )

            // Exact alarm (Android 12+): needed for on-time prayer notifications
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )
                PermissionSwitch(
                    title = stringResource(R.string.exact_alarm),
                    description = stringResource(R.string.allow_exact_alarm_for_prayer_time),
                    checked = isExactAlarmEnable,
                    onCheckedChange = { onGrantExactAlarm() },
                )
            }
        }
    }
}


@Composable
private fun PermissionSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CustomSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            switchColor = ColorBlue,
            disableBackgroundColor = Color.White.copy(alpha = 0.15f),
            thumbColor = Color.White,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )

            Text(
                text = description,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
private fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 42.dp,
    height: Dp = 24.dp,
    switchColor: Color = Color.Green,
    disableBackgroundColor: Color = Color.LightGray,
    enableBackgroundColor: Brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2ACDCD), Color(0xFFF236F5))
    ),
    thumbColor: Color = Color.White
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) width - height else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "ThumbOffset"
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(CircleShape)
            .then(
                if (checked) {
                    Modifier.background(switchColor)
                } else {
                    Modifier.background(disableBackgroundColor)
                }
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(height)
                .offset(x = thumbOffset)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PhotosPermissionBottomSheetLayout()
}