package com.novah.mathmind.data

import com.novah.mathmind.R
import com.novah.mathmind.data.dao.CustomGameDao
import com.novah.mathmind.data.entities.CustomGame
import com.novah.mathmind.games.mathchallenge.MathChallengeActivity
import com.novah.mathmind.games.sudoku.SudokuActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository class to abstract access to game data.
 * Combines built-in games with dynamically added custom games.
 */
class GameRepository(private val customGameDao: CustomGameDao) {

    // List of predefined built-in games
    private val builtInGames = listOf(
        Game.BuiltInGame(
            id = "math_challenge",
            title = "Math Challenge",
            description = "Test your arithmetic skills with timed equations.",
            iconResId = R.drawable.ic_math_challenge,
            activityClass = MathChallengeActivity::class.java
        ),
        Game.BuiltInGame(
            id = "sudoku",
            title = "Sudoku",
            description = "Solve classic number puzzles.",
            iconResId = R.drawable.ic_sudoku,
            activityClass = SudokuActivity::class.java
        )
    )

    /**
     * Provides a Flow of all games (built-in + custom).
     * Custom games are fetched from the Room database.
     */
    fun getAllGames(): Flow<List<Game>> {
        return customGameDao.getAllCustomGames().map { customGames ->
            val customGameItems = customGames.map { customGame ->
                Game.CustomGameItem(
                    id = customGame.id.toString(), // Convert Room ID to String for generic Game ID
                    title = customGame.title,
                    description = "Custom game added by developer.",
                    iconResId = R.drawable.ic_custom_game
                )
            }
            builtInGames + customGameItems
        }
    }

    /**
     * Inserts a new custom game into the database.
     */
    suspend fun insertCustomGame(customGame: CustomGame) {
        customGameDao.insert(customGame)
    }

    /**
     * Retrieves a custom game by its ID.
     */
    suspend fun getCustomGameById(id: Long): CustomGame? {
        return customGameDao.getCustomGameById(id)
    }
}
