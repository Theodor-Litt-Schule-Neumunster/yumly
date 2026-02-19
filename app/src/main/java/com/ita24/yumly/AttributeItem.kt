package com.ita24.yumly

import androidx.annotation.DrawableRes

data class AttributeItem(
    val name: String,
    @DrawableRes val drawableId: Int,
    var isSelected: Boolean = false
)