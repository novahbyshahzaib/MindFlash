package com.novah.mathmind.games.mathchallenge

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.novah.mathmind.R
import com.novah.mathmind.databinding.ActivityMathChallengeBinding

/**
 * Activity for the Math Challenge game.
 * Manages UI interaction and observes game state from MathChallengeViewModel.
 */
class MathChallengeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMathChallengeBinding
    private val viewModel: MathChallengeViewModel by viewModels() // ViewModel scoped to this activity
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMathChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Math Challenge"

        setupDifficultySelection()
        setupObservers()
        setupListeners()

        // Start with default difficulty (Easy)
        viewModel.setDifficulty(MathChallengeViewModel.Difficulty.EASY)
    }

    /**
     * Sets up radio buttons for difficulty selection.
     */
    private fun setupDifficultySelection() {
        binding.radioGroupDifficulty.setOnCheckedChangeListener { _, checkedId ->
            val difficulty = when (checkedId) {
                R.id.radio_easy -> MathChallengeViewModel.Difficulty.EASY
                R.id.radio_medium -> MathChallengeViewModel.Difficulty.MEDIUM
                R.id.radio_hard -> MathChallengeViewModel.Difficulty.HARD
                else -> MathChallengeViewModel.Difficulty.EASY
            }
            viewModel.setDifficulty(difficulty)
        }
        // Set default selection
        binding.radioEasy.isChecked = true
    }

    /**
     * Observes LiveData from the ViewModel to update the UI.
     */
    private fun setupObservers() {
        viewModel.equation.observe(this) { equation ->
            binding.textQuestion.text = equation
            binding.editAnswer.setText("") // Clear answer input for new question
            binding.editAnswer.requestFocus() // Focus on the answer field
        }

        viewModel.score.observe(this) { score ->
            binding.textScore.text = getString(R.string.math_challenge_score, score)
        }

        viewModel.timer.observe(this) { timeLeft ->
            binding.textTimer.text = getString(R.string.math_challenge_timer, timeLeft)
            // Change timer color based on remaining time (e.g., red when low)
            if (timeLeft <= 5) {
                binding.textTimer.setTextColor(ContextCompat.getColor(this, R.color.red_500))
            } else {
                binding.textTimer.setTextColor(ContextCompat.getColor(this, R.color.design_default_color_on_surface))
            }
        }

        viewModel.isGameOver.observe(this) { isGameOver ->
            if (isGameOver) {
                showGameOverDialog()
            }
        }
    }

    /**
     * Sets up listeners for the submit button and answer input.
     */
    private fun setupListeners() {
        binding.buttonSubmit.setOnClickListener {
            checkAnswer()
        }

        binding.buttonStartRestart.setOnClickListener {
            viewModel.startGame()
            binding.editAnswer.isEnabled = true
            binding.buttonSubmit.isEnabled = true
            binding.radioGroupDifficulty.isEnabled = false // Disable difficulty change during game
            for (i in 0 until binding.radioGroupDifficulty.childCount) {
                binding.radioGroupDifficulty.getChildAt(i).isEnabled = false
            }
            binding.buttonStartRestart.text = getString(R.string.math_challenge_restart)
        }

        // Allow submitting answer by pressing Enter on the keyboard
        binding.editAnswer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer()
                true
            } else {
                false
            }
        }
    }

    /**
     * Checks the user's answer and updates the ViewModel.
     * Provides visual feedback (correct/incorrect).
     */
    private fun checkAnswer() {
        val userAnswer = binding.editAnswer.text.toString().trim()
        if (userAnswer.isEmpty()) {
            Snackbar.make(binding.root, "Please enter an answer", Snackbar.LENGTH_SHORT).show()
            return
        }

        val isCorrect = viewModel.submitAnswer(userAnswer.toIntOrNull())
        showFeedback(isCorrect)
    }

    /**
     * Shows visual feedback (green for correct, red for incorrect) using a background change.
     */
    private fun showFeedback(isCorrect: Boolean) {
        val originalColor = binding.textQuestion.currentTextColor
        val feedbackColor = if (isCorrect) ContextCompat.getColor(this, R.color.green_500) else ContextCompat.getColor(this, R.color.red_500)

        binding.textQuestion.setTextColor(feedbackColor)
        handler.postDelayed({
            binding.textQuestion.setTextColor(originalColor)
        }, 300) // Revert color after a short delay
    }

    /**
     * Displays a dialog when the game is over, showing the final score.
     */
    private fun showGameOverDialog() {
        val finalScore = viewModel.score.value ?: 0
        Snackbar.make(
            binding.root,
            "Game Over! Your final score: $finalScore",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("Restart") {
                viewModel.startGame()
            }
            .show()

        // Disable input after game over
        binding.editAnswer.isEnabled = false
        binding.buttonSubmit.isEnabled = false
        binding.radioGroupDifficulty.isEnabled = true // Re-enable difficulty selection
        for (i in 0 until binding.radioGroupDifficulty.childCount) {
            binding.radioGroupDifficulty.getChildAt(i).isEnabled = true
        }
        binding.buttonStartRestart.text = getString(R.string.math_challenge_start)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
