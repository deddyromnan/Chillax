package com.romnan.chillax.core.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes

data class Sound(
    @DrawableRes val icon: Int,
    @StringRes val name: Int,
    @RawRes val resource: Int,
)