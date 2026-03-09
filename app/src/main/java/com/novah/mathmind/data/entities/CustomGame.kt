package com.novah.mathmind.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for a dynamically added custom game.
 * Stores the HTML, CSS, and JavaScript code for the WebView.
 */
@Entity(tableName = "custom_games")
data class CustomGame(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val htmlCode: String,
    val cssCode: String,
    val jsCode: String
)
