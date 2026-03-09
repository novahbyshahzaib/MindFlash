package com.novah.mathmind.games.sudoku

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.snackbar.Snackbar
import com.novah.mathmind.R
import com.novah.mathmind.databinding.ActivitySudokuBinding

/**
 * Activity for the Sudoku game.
 * Manages UI for the Sudoku board, user input, and observes game state from SudokuViewModel.
 */
class SudokuActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySudokuBinding
    private val viewModel: SudokuViewModel by viewModels()
    private var selectedCell: Pair<Int, Int>? = null // (row, col)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySudokuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Sudoku"

        setupBoardUI()
        setupNumberInput()
        setupObservers()
        setupControlButtons()

        // Start a new game on creation
        viewModel.newGame()
    }

    /**
     * Dynamically creates the Sudoku board UI using TextViews within a GridLayout.
     */
    private fun setupBoardUI() {
        val cellSize = resources.displayMetrics.widthPixels / 9 - 4 // Adjust for padding/margins
        val padding = 2

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val cell = TextView(this).apply {
                    width = cellSize
                    height = cellSize
                    gravity = Gravity.CENTER
                    textSize = 20f
                    id = View.generateViewId() // Generate a unique ID for each TextView
                    setPadding(padding, padding, padding, padding)
                    setBackgroundResource(R.drawable.sudoku_cell_background) // Custom drawable for cell borders
                    setOnClickListener { onCellSelected(row, col) }
                }

                val params = binding.sudokuGrid.layoutParams as ViewGroup.MarginLayoutParams
                binding.sudokuGrid.addView(cell)
            }
        }
    }


    /**
     * Sets up click listeners for number input buttons (1-9 and Clear).
     */
    private fun setupNumberInput() {
        binding.numPad.children.filterIsInstance<TextView>().forEach { textView ->
            textView.setOnClickListener {
                when (it.id) {
                    R.id.btn_clear -> viewModel.updateCell(selectedCell, 0)
                    else -> (it as? TextView)?.text?.toString()?.toIntOrNull()?.let { num ->
                        viewModel.updateCell(selectedCell, num)
                    }
                }
            }
        }
    }

    /**
     * Observes LiveData from the ViewModel to update the Sudoku board.
     */
    private fun setupObservers() {
        viewModel.board.observe(this) { board ->
            updateBoardUI(board)
        }

        viewModel.selectedCell.observe(this) { pair ->
            // Update previous selected cell's background
            selectedCell?.let { (r, c) ->
                val prevCellView = getCellView(r, c)
                prevCellView?.setBackgroundResource(R.drawable.sudoku_cell_background)
            }

            // Update current selected cell's background
            pair?.let { (r, c) ->
                val currentCellView = getCellView(r, c)
                currentCellView?.setBackgroundResource(R.drawable.sudoku_cell_background_selected)
            }
            selectedCell = pair
        }

        viewModel.isGameWon.observe(this) { isWon ->
            if (isWon) {
                Snackbar.make(binding.root, "Congratulations! You solved the Sudoku!", Snackbar.LENGTH_LONG)
                    .setAction("New Game") { viewModel.newGame() }
                    .show()
            }
        }
    }

    /**
     * Sets up click listeners for New Game and Solve buttons.
     */
    private fun setupControlButtons() {
        binding.btnNewGame.setOnClickListener {
            viewModel.newGame()
            selectedCell = null // Clear selection
        }
        binding.btnSolve.setOnClickListener {
            viewModel.solvePuzzle()
            selectedCell = null // Clear selection
        }
    }

    /**
     * Updates the UI of the Sudoku board based on the current board state from the ViewModel.
     * Highlights initial cells and user-entered cells differently.
     */
    private fun updateBoardUI(board: Array<IntArray>) {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val cellView = getCellView(row, col)
                val value = board[row][col]

                cellView?.apply {
                    text = if (value != 0) value.toString() else ""

                    // Style for initial fixed numbers
                    if (viewModel.isInitialCell(row, col)) {
                        setTextColor(ContextCompat.getColor(context, R.color.design_default_color_on_surface))
                        setTypeface(null, Typeface.BOLD)
                    } else {
                        // Style for user-entered numbers
                        setTextColor(ContextCompat.getColor(context, R.color.blue_500))
                        setTypeface(null, Typeface.NORMAL)
                    }

                    // Reset background for non-selected cells
                    if (selectedCell == null || selectedCell!!.first != row || selectedCell!!.second != col) {
                        setBackgroundResource(R.drawable.sudoku_cell_background)
                    }
                }
            }
        }
    }

    /**
     * Handles a cell click, updating the selected cell in the ViewModel.
     */
    private fun onCellSelected(row: Int, col: Int) {
        viewModel.selectCell(row, col)
    }

    /**
     * Helper to get the TextView for a specific cell from the GridLayout.
     */
    private fun getCellView(row: Int, col: Int): TextView? {
        val index = row * 9 + col
        return binding.sudokuGrid.getChildAt(index) as? TextView
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
