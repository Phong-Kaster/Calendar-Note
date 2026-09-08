package com.example.skeleton.ui.component.ratebottomsheet

import androidx.annotation.RawRes
import com.example.skeleton.R

enum class RateOption(
    @get:RawRes val emoji: Int
) {
    ONE_STARS(emoji = R.raw.animation_1_star),
    TWO_STARS(emoji = R.raw.animation_2_stars),
    THREE_STARS(emoji = R.raw.animation_3_stars),
    FOUR_STARS(emoji = R.raw.animation_4_stars),
    FIVE_STARS(emoji = R.raw.animation_5_stars),
    ;

    companion object {
        fun getByPosition(position: Int): Int {
            return entries
                .getOrNull(position - 1)
                ?.emoji
                ?: R.raw.animation_5_stars
        }

    }
}