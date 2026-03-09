package com.novah.mathmind.games.mathchallenge

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel for the Math Challenge game.
 * Manages game state, equation generation, scoring, and timer logic.
 */
class MathChallengeViewModel : ViewModel() {

    // LiveData for UI updates
    private val _equation = MutableLiveData<String>()
    val equation: LiveData<String> = _equation

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _timer = MutableLiveData<Int>()
    val timer: LiveData<Int> = _timer

    private val _isGameOver = MutableLiveData(false)
    val isGameOver: LiveData<Boolean> = _isGameOver

    private var currentDifficulty: Difficulty = Difficulty.EASY
    private var currentAnswer: Int? = null
    private var countDownTimer: CountDownTimer? = null

    // Enum for game difficulty
    enum class Difficulty(val maxOperand: Int, val timeLimitSeconds: Int) {
        EASY(10, 15),       // e.g., 1-10, 15 seconds per question
        MEDIUM(50, 10),     // e.g., 1-50, 10 seconds
        HARD(100, 7)        // e.g., 1-100, 7 seconds
    }

    init {
        // Initialize game state
        resetGame()
    }

    /**
     * Sets the current difficulty for the game.
     * Resets the game to apply new difficulty settings.
     */
    fun setDifficulty(difficulty: Difficulty) {
        currentDifficulty = difficulty
        resetGame()
    }

    /**
     * Starts or restarts the game.
     * Generates the first equation and starts the timer.
     */
    fun startGame() {
        resetGame()
        generateNewEquation()
        startTimer()
    }

    /**
     * Resets the game state to initial values.
     * Stops any active timer.
     */
    private fun resetGame() {
        countDownTimer?.cancel()
        _score.value = 0
        _isGameOver.value = false
        _timer.value = currentDifficulty.timeLimitSeconds
        currentAnswer = null
    }

    /**
     * Generates a new random math equation based on the current difficulty.
     * Updates the [_equation] LiveData and stores the correct answer.
     */
    private fun generateNewEquation() = viewModelScope.launch {
        val operand1 = Random.nextInt(1, currentDifficulty.maxOperand + 1)
        val operand2 = Random.nextInt(1, currentDifficulty.maxOperand + 1)
        val operator = arrayOf("+", "-", "*", "/").random() // Select a random operator

        val question: String
        val answer: Int

        when (operator) {
            "+" -> {
                question = "$operand1 + $operand2"
                answer = operand1 + operand2
            }
            "-" -> {
                // Ensure result is non-negative for subtraction (simple approach)
                val (num1, num2) = if (operand1 >= operand2) Pair(operand1, operand2) else Pair(operand2, operand1)
                question = "$num1 - $num2"
                answer = num1 - num2
            }
            "*" -> {
                question = "$operand1 * $operand2"
                answer = operand1 * operand2
            }
            "/" -> {
                // Ensure division results in a whole number (no remainder)
                var op1 = operand1
                var op2 = operand2
                if (op2 == 0) op2 = 1 // Avoid division by zero
                while (op1 % op2 != 0 || op1 < op2) {
                    op1 = Random.nextInt(1, currentDifficulty.maxOperand * 2 + 1) // Increase range for better division
                    op2 = Random.nextInt(1, currentDifficulty.maxOperand + 1)
                    if (op2 == 0) op2 = 1
                }
                question = "$op1 / $op2"
                answer = op1 / op2
            }
            else -> { // Should not happen
                question = "Error"
                answer = 0
            }
        }
        _equation.value = question
        currentAnswer = answer
        startTimer() // Restart timer for new question
    }

    /**
     * Starts the countdown timer for the current question.
     * If time runs out, the game is over.
     */
    private fun startTimer() {
        countDownTimer?.cancel() // Cancel any existing timer
        _timer.value = currentDifficulty.timeLimitSeconds

        countDownTimer = object : CountDownTimer(currentDifficulty.timeLimitSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _timer.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _timer.value = 0
                _isGameOver.value = true // Game over if time runs out
            }
        }.start()
    }

    /**
     * Submits the user's answer.
     * Checks if it's correct, updates score, and generates a new equation.
     *
     * @param userAnswer The integer answer provided by the user.
     * @return True if the answer was correct, false otherwise.
     */
    fun submitAnswer(userAnswer: Int?): Boolean {
        if (_isGameOver.value == true) return false // Cannot submit if game is over

        val correct = userAnswer != null && userAnswer == currentAnswer
        if (correct) {
            _score.value = (_score.value ?: 0) + 1
        }
        generateNewEquation() // Always generate new question regardless of correctness
        return correct
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel() // Ensure timer is cancelled when ViewModel is destroyed
    }
}
