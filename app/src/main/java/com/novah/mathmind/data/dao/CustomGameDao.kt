package com.novah.mathmind.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.novah.mathmind.data.entities.CustomGame
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CustomGame entities.
 * Handles database operations for dynamically added games.
 */
@Dao
interface CustomGameDao {
    @Query("SELECT * FROM custom_games ORDER BY title ASC")
    fun getAllCustomGames(): Flow<List<CustomGame>>

    @Query("SELECT * FROM custom_games WHERE id = :gameId")
    suspend fun getCustomGameById(gameId: Long): CustomGame?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customGame: CustomGame): Long // Returns the new row ID
}
