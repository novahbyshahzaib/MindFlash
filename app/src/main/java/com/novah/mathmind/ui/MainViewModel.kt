package com.novah.mathmind.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.novah.mathmind.data.GameRepository

/**
 * ViewModel for MainActivity/HomeFragment.
 * Provides access to the list of available games.
 */
class MainViewModel(private val repository: GameRepository) : ViewModel() {

    // Expose the combined list of built-in and custom games as LiveData
    val allGames = repository.getAllGames().asLiveData()
}

/**
 * Factory for MainViewModel to allow dependency injection of GameRepository.
 */
class MainViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
