package com.novah.mathmind.ui.developer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.novah.mathmind.NovahMathMindApplication
import com.novah.mathmind.data.entities.CustomGame
import com.novah.mathmind.databinding.FragmentAddGameBinding

/**
 * Fragment for adding new custom games using HTML, CSS, and JavaScript code.
 */
class AddGameFragment : Fragment() {

    private var _binding: FragmentAddGameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddGameViewModel by viewModels {
        AddGameViewModelFactory((activity?.application as NovahMathMindApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSaveGame.setOnClickListener {
            saveGame()
        }
    }

    /**
     * Gathers input from EditText fields, validates it, and saves the custom game to the database.
     */
    private fun saveGame() {
        val title = binding.editTextGameTitle.text.toString().trim()
        val htmlCode = binding.editTextHtmlCode.text.toString().trim()
        val cssCode = binding.editTextCssCode.text.toString().trim()
        val jsCode = binding.editTextJsCode.text.toString().trim()

        if (title.isEmpty() || htmlCode.isEmpty()) {
            Snackbar.make(binding.root, "Title and HTML code are required.", Snackbar.LENGTH_SHORT).show()
            return
        }

        val customGame = CustomGame(
            title = title,
            htmlCode = htmlCode,
            cssCode = cssCode,
            jsCode = jsCode
        )

        viewModel.insertCustomGame(customGame)
        Snackbar.make(binding.root, "Game '$title' added!", Snackbar.LENGTH_SHORT).show()
        findNavController().popBackStack() // Navigate back to Developer Options
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
