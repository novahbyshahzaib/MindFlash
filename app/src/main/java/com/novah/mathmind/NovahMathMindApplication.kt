package com.novah.mathmind

import android.app.Application
import com.novah.mathmind.data.AppDatabase
import com.novah.mathmind.data.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Custom Application class for NovahMathMind.
 * Initializes the Room database and provides a repository singleton.
 */
class NovahMathMindApplication : Application() {

    // Using a Singleton scope for the application's lifetime
    val applicationScope = CoroutineScope(SupervisorJob())

    // Lazy initialization for the database and repository
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { GameRepository(database.customGameDao()) }
}
