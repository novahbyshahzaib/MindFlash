package com.novah.mathmind.ui.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.novah.mathmind.data.GameRepository
import com.novah.mathmind.data.entities.CustomGame
import kotlinx.coroutines.launch

/**
 * ViewModel for AddGameFragment.
 * Handles insertion and retrieval of CustomGame entities.
 */
class AddGameViewModel(private val repository: GameRepository) : ViewModel() {

    /**
     * Inserts a new custom game into the database.
     */
    fun insertCustomGame(customGame: CustomGame) = viewModelScope.launch {
        repository.insertCustomGame(customGame)
    }

    /**
     * Retrieves a custom game by its ID.
     */
    suspend fun getCustomGameById(id: Long): CustomGame? {
        return repository.getCustomGameById(id)
    }
}

/**
 * Factory for AddGameViewModel to allow dependency injection of GameRepository.
 */
class AddGameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddGameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
