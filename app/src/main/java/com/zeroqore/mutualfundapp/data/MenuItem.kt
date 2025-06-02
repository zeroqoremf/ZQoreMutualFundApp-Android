// app/src/main/java/com/zeroqore/mutualfundapp/data/MenuItem.kt
package com.zeroqore.mutualfundapp.data

// This data class defines a single menu item as used in your app
data class MenuItem(
    val id: String,
    val title: String,
    val description: String? = null // Optional description for the menu item
    // Add an icon resource ID (e.g., val iconResId: Int? = null) if you use icons
)