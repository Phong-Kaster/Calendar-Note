package com.example.skeleton.domain.enums

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.example.skeleton.R

/**
 * The app's top-level screens, in the order the bottom bar shows them.
 *
 * **Declaration order is layout order.** `CoreBottomBar` splits this list down the middle and
 * hangs the halves either side of the centre action button, so moving an entry here moves the tab
 * on screen. Adding one is the whole job of putting a new top-level screen in the bar.
 *
 * @author Phong-Kaster
 */
enum class BottomBarDestination (
    @StringRes val nameId: Int,
    @DrawableRes val drawableId: Int,
    @IdRes val destinationId: Int,
    val directions: Int,
    @IdRes val homeDestinationId: Int,
) {
    Home(
        nameId = R.string.home,
        drawableId = R.drawable.ic_bottom_home,
        destinationId = R.id.homeFragment,
        directions = R.id.toHome,
        homeDestinationId = R.id.homeFragment,
    ),
    Calendar(
        nameId = R.string.calendar,
        drawableId = R.drawable.ic_bottom_calendar,
        destinationId = R.id.calendarFragment,
        directions = R.id.toCalendar,
        homeDestinationId = R.id.calendarFragment,
    ),
    Alarms(
        nameId = R.string.alarms,
        drawableId = R.drawable.ic_bottom_alarm,
        destinationId = R.id.alarmsFragment,
        directions = R.id.toAlarms,
        homeDestinationId = R.id.alarmsFragment,
    ),
    Setting(
        nameId = R.string.setting,
        drawableId = R.drawable.ic_bottom_settings,
        destinationId = R.id.settingFragment,
        directions = R.id.toSetting,
        homeDestinationId = R.id.settingFragment,
    ),
}
