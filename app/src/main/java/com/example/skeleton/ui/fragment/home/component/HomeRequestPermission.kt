package com.example.skeleton.ui.fragment.home.component

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.skeleton.ui.util.LogUtil
import com.example.skeleton.ui.util.PermissionUtil.isLocationGranted
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeRequestPermission(
    enable: Int,
    onNotificationGranted: () -> Unit = {},
    onLocationGranted: () -> Unit = {},
    onExactAlarmGranted: () -> Unit = {}
) {
    val context = LocalContext.current

    var showBottomSheet by remember { mutableStateOf(false) }

    val notificationPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    var hasRequestedNotification by rememberSaveable { mutableStateOf(false) }
    var hasRequestedLocation by rememberSaveable { mutableStateOf(false) }

    var exactAlarmGranted by remember { mutableStateOf(canScheduleExactAlarm(context)) }

    /**
     * Re-check permissions whenever [enable] changes and decide whether to show bottom sheet.
     */
    LaunchedEffect(enable) {
        val notificationGranted = isNotificationGranted(context)
        val locationGranted = isLocationGranted(context)
        showBottomSheet = !notificationGranted || !locationGranted || !exactAlarmGranted
    }

    /**
     * Re-check when returning from Settings (e.g. user enabled exact alarm or granted permission in app settings).
     */
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            val notificationGranted = isNotificationGranted(context)
            val locationGranted = isLocationGranted(context)
            exactAlarmGranted = canScheduleExactAlarm(context)
            if (exactAlarmGranted) onExactAlarmGranted()
            showBottomSheet = !notificationGranted || !locationGranted || !exactAlarmGranted
        }
    }

    /**
     * When user grants notification from in-app dialog (without going to Settings), update UI and callback.
     */
    LaunchedEffect(notificationPermission.status) {
        if (notificationPermission.status is PermissionStatus.Granted) {
            onNotificationGranted()
        }
    }

    /**
     * When user grants location from in-app dialog, update UI and callback.
     */
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            onLocationGranted()
        }
    }

    /**
     * Keep bottom sheet visibility in sync with current permission state (e.g. after in-app grant).
     */
    LaunchedEffect(
        notificationPermission.status,
        locationPermissions.allPermissionsGranted,
        exactAlarmGranted
    ) {
        val notificationGranted = isNotificationGranted(context)
        val locationGranted = locationPermissions.allPermissionsGranted
        showBottomSheet = !notificationGranted || !locationGranted || !exactAlarmGranted
    }



    /**
     * Notification permission launcher (Android 13+)
     */
    fun requestNotification() {
        when (val status = notificationPermission.status) {

            PermissionStatus.Granted -> {
                LogUtil.logcat("notification granted", "Notification")
                onNotificationGranted()
            }

            is PermissionStatus.Denied -> {

                if (status.shouldShowRationale) {
                    LogUtil.logcat(
                        "User denied once, show system dialog again",
                        "notification"
                    )

                    notificationPermission.launchPermissionRequest()
                } else {

                    if (!hasRequestedNotification) {

                        // First request
                        hasRequestedNotification = true
                        notificationPermission.launchPermissionRequest()

                    } else {
                        // User denied twice or selected "Don't ask again"
                        LogUtil.logcat(
                            "Permission permanently denied -> open settings",
                            "notification"
                        )

                        openAppSettings(context)
                    }
                }
            }
        }
    }

    /**
     * Request Location
     */
    fun requestLocation() {

        if (locationPermissions.allPermissionsGranted) {
            onLocationGranted()
            return
        }

        if (locationPermissions.shouldShowRationale) {

            LogUtil.logcat(
                "User denied once -> show permission dialog again",
                "location"
            )

            locationPermissions.launchMultiplePermissionRequest()
            return
        }

        if (!hasRequestedLocation) {

            LogUtil.logcat(
                "First time requesting location permission",
                "location"
            )

            hasRequestedLocation = true
            locationPermissions.launchMultiplePermissionRequest()
            return
        }

        LogUtil.logcat(
            "Permission permanently denied -> open settings",
            "location"
        )

        openAppSettings(context)
    }

    /**
     * Request Exact Alarm (Android 12+). Opens app settings so user can enable "Alarms & reminders".
     */
    fun requestExactAlarm() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return


        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
        if (alarmManager?.canScheduleExactAlarms() == false) {
            val intent = Intent()
            intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM

            try {
                context.startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * Bottom Sheet
     */
    HomePermissionBottomSheet(
        enable = showBottomSheet,
        isNotificationEnable = isNotificationGranted(context),
        isLocationEnable = isLocationGranted(context),
        isExactAlarmEnable = exactAlarmGranted,
        onDismiss = { showBottomSheet = false },
        onGrantNotification = { requestNotification() },
        onGrantLocation = { requestLocation() },
        onGrantExactAlarm = { requestExactAlarm() },
    )
}

/**
 * Returns true if the app can schedule exact alarms (Android 12+). Pre-S always true.
 * Used by HomeFragment to trigger permission UI and by this composable for bottom sheet state.
 */
fun canScheduleExactAlarm(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}


/**
 * Check if we should show permission rationale.
 */
private fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
    return if (context is FragmentActivity) {
        ActivityCompat.shouldShowRequestPermissionRationale(
            context,
            permission
        )
    } else {
        false
    }
}

fun isNotificationGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}


fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}