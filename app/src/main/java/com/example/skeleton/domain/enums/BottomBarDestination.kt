package com.example.skeleton.domain.enums

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.example.skeleton.R

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
    Setting(
        nameId = R.string.setting,
        drawableId = R.drawable.ic_bottom_settings,
        destinationId = R.id.settingFragment,
        directions = R.id.toSetting,
        homeDestinationId = R.id.settingFragment,
    ),
}
