package com.novah.mathmind.data

import androidx.annotation.DrawableRes
import com.novah.mathmind.R

/**
 * Sealed class representing a generic game item displayed on the homepage.
 * It can be either a built-in game or a dynamically added custom game.
 */
sealed class Game {
    abstract val id: String
    abstract val title: String
    abstract val description: String
    @get:DrawableRes abstract val iconResId: Int

    data class BuiltInGame(
        override val id: String,
        override val title: String,
        override val description: String,
        @DrawableRes override val iconResId: Int,
        val activityClass: Class<*> // For built-in games, we need to know which Activity to launch
    ) : Game()

    data class CustomGameItem(
        override val id: String,
        override val title: String,
        override val description: String,
        @DrawableRes override val iconResId: Int = R.drawable.ic_custom_game // Default icon for custom games
    ) : Game()
}
