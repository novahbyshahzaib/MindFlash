package com.novah.mathmind.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.novah.mathmind.data.dao.CustomGameDao
import com.novah.mathmind.data.entities.CustomGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The Room database for NovahMathMind.
 * Stores CustomGame entities.
 */
@Database(entities = [CustomGame::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customGameDao(): CustomGameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            // If the INSTANCE is not null, then return it,
            // otherwise, create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "novah_mathmind_db"
                )
                    .addCallback(AppDatabaseCallback(scope)) // Optional: Add a callback for initial data
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Optional callback to populate the database on first creation.
     * Not strictly needed for this app, but good practice.
     */
    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    // Populate database if needed, e.g., default custom games.
                    // For now, leave empty as games are added via UI.
                }
            }
        }
    }
}
