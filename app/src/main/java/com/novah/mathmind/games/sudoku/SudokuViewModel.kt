package com.novah.mathmind.games.sudoku

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * ViewModel for the Sudoku game.
 * Manages the Sudoku board state, user input, validation, and game logic.
 */
class SudokuViewModel : ViewModel() {

    // LiveData for the current Sudoku board state
    private val _board = MutableLiveData<Array<IntArray>>()
    val board: LiveData<Array<IntArray>> = _board

    // LiveData for the currently selected cell
    private val _selectedCell = MutableLiveData<Pair<Int, Int>?>()
    val selectedCell: LiveData<Pair<Int, Int>?> = _selectedCell

    // LiveData to indicate if the game has been won
    private val _isGameWon = MutableLiveData(false)
    val isGameWon: LiveData<Boolean> = _isGameWon

    private lateinit var initialBoard: Array<IntArray> // Stores the puzzle without user inputs

    init {
        // Initialize with an empty board or a default puzzle
        _board.value = Array(9) { IntArray(9) }
    }

    /**
     * Starts a new Sudoku game by generating a new puzzle.
     */
    fun newGame() = viewModelScope.launch(Dispatchers.Default) {
        val newPuzzle = generateSudokuPuzzle()
        initialBoard = Array(9) { r -> IntArray(9) { c -> newPuzzle[r][c] } } // Deep copy
        withContext(Dispatchers.Main) {
            _board.value = Array(9) { r -> IntArray(9) { c -> newPuzzle[r][c] } } // Deep copy
            _selectedCell.value = null
            _isGameWon.value = false
        }
    }

    /**
     * Selects a cell on the board.
     * @param row The row index of the cell.
     * @param col The column index of the cell.
     */
    fun selectCell(row: Int, col: Int) {
        _selectedCell.value = Pair(row, col)
    }

    /**
     * Updates the value of the currently selected cell.
     * Only allows updates if the cell is not part of the initial puzzle.
     *
     * @param cell The (row, col) pair of the cell to update.
     * @param value The new value (0 for clear, 1-9 for numbers).
     */
    fun updateCell(cell: Pair<Int, Int>?, value: Int) {
        if (cell == null || isInitialCell(cell.first, cell.second)) {
            return // Cannot update initial cells or if no cell is selected
        }

        val (row, col) = cell
        val currentBoard = _board.value?.map { it.clone() }?.toTypedArray() ?: return

        currentBoard[row][col] = value
        _board.value = currentBoard // Update LiveData

        // Check for win condition after every valid update
        _isGameWon.value = checkWinCondition(currentBoard)
    }

    /**
     * Checks if a given cell is part of the initial puzzle (not user-editable).
     */
    fun isInitialCell(row: Int, col: Int): Boolean {
        return initialBoard[row][col] != 0
    }

    /**
     * Attempts to solve the current Sudoku puzzle.
     * This is a basic backtracking solver.
     */
    fun solvePuzzle() = viewModelScope.launch(Dispatchers.Default) {
        val currentBoard = _board.value?.map { it.clone() }?.toTypedArray() ?: return@launch
        if (solveSudoku(currentBoard)) {
            withContext(Dispatchers.Main) {
                _board.value = currentBoard
                _isGameWon.value = true
            }
        } else {
            // Could not solve (shouldn't happen with valid generated puzzles)
            // TODO: Provide user feedback if unsolvable
        }
    }

    /**
     * Backtracking algorithm to solve the Sudoku puzzle.
     */
    private fun solveSudoku(board: Array<IntArray>): Boolean {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (board[r][c] == 0) { // Find empty cell
                    for (num in 1..9) {
                        if (isValidMove(board, r, c, num)) {
                            board[r][c] = num
                            if (solveSudoku(board)) {
                                return true
                            } else {
                                board[r][c] = 0 // Backtrack
                            }
                        }
                    }
                    return false // No number works for this cell
                }
            }
        }
        return true // All cells filled
    }

    /**
     * Checks if placing a number at a given cell is valid according to Sudoku rules.
     */
    private fun isValidMove(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // Check row
        for (x in 0 until 9) {
            if (board[row][x] == num && x != col) return false
        }
        // Check column
        for (x in 0 until 9) {
            if (board[x][col] == num && x != row) return false
        }
        // Check 3x3 box
        val startRow = row - row % 3
        val startCol = col - col % 3
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i + startRow][j + startCol] == num && (i + startRow != row || j + startCol != col)) return false
            }
        }
        return true
    }

    /**
     * Checks if the entire board is filled correctly, indicating a win.
     */
    private fun checkWinCondition(board: Array<IntArray>): Boolean {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val value = board[r][c]
                if (value == 0 || !isValidMove(board, r, c, value)) {
                    return false // Empty cell or invalid move
                }
            }
        }
        return true // All cells are filled and valid
    }

    /**
     * Generates a solvable Sudoku puzzle.
     * This is a simplified approach:
     * 1. Create a full solved Sudoku board.
     * 2. Remove a certain number of cells to create the puzzle.
     * (A more robust generator would ensure a unique solution, but this is sufficient for demo).
     */
    private fun generateSudokuPuzzle(): Array<IntArray> {
        val solutionBoard = Array(9) { IntArray(9) }
        fillDiagonalBoxes(solutionBoard) // Fill 3x3 diagonal boxes first
        solveSudoku(solutionBoard) // Then solve the rest to get a full valid board

        // Create the puzzle by removing numbers
        val puzzleBoard = solutionBoard.map { it.clone() }.toTypedArray()
        var cellsToRemove = 40 // Adjust difficulty here (more cells removed = harder)
        while (cellsToRemove > 0) {
            val row = Random.nextInt(9)
            val col = Random.nextInt(9)
            if (puzzleBoard[row][col] != 0) {
                puzzleBoard[row][col] = 0
                cellsToRemove--
            }
        }
        return puzzleBoard
    }

    /**
     * Fills the three 3x3 diagonal boxes with random numbers, ensuring validity within the box.
     * This helps in starting the [solveSudoku] algorithm with a partially filled valid board.
     */
    private fun fillDiagonalBoxes(board: Array<IntArray>) {
        for (i in 0 until 9 step 3) {
            fillBox(board, i, i)
        }
    }

    /**
     * Fills a specific 3x3 box with random unique numbers.
     */
    private fun fillBox(board: Array<IntArray>, row: Int, col: Int) {
        val nums = (1..9).toMutableList()
        nums.shuffle()
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                board[row + i][col + j] = nums.removeAt(0)
            }
        }
    }
}
