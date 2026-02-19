package com.ita24.yumly

import androidx.annotation.DrawableRes

/**
 * Data class representing a single selectable attribute icon.
 *
 * @param name The unique identifier for the attribute (e.g., "veggie_att").
 * @param drawableId The resource ID of the icon drawable.
 * @param isSelected A flag to track if the item is currently selected.
 */
data class AttributeItem(
    val name: String,
    @DrawableRes val drawableId: Int,
    var isSelected: Boolean = false
)
