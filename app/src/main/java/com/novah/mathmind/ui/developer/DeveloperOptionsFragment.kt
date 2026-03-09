package com.novah.mathmind.ui.developer

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.novah.mathmind.R
import com.novah.mathmind.databinding.FragmentDeveloperOptionsBinding

/**
 * Fragment for hidden Developer Options.
 * Requires passcode authentication.
 */
class DeveloperOptionsFragment : Fragment() {

    private var _binding: FragmentDeveloperOptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "NovahMathMindPrefs"
        private const val KEY_DEVELOPER_MODE_ENABLED = "developer_mode_enabled"
        private const val DEVELOPER_PASSCODE = "NOVAH HOST"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeveloperOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if developer mode is already enabled
        val isDeveloperModeEnabled = sharedPreferences.getBoolean(KEY_DEVELOPER_MODE_ENABLED, false)
        if (!isDeveloperModeEnabled) {
            // If not enabled, show passcode dialog
            showPasscodeDialog()
        } else {
            // If already enabled, show options directly
            showDeveloperOptions()
        }
    }

    /**
     * Displays an AlertDialog to prompt the user for the developer passcode.
     */
    private fun showPasscodeDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Enter passcode"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Developer Passcode Required")
            .setView(input)
            .setPositiveButton("Verify") { dialog, _ ->
                val enteredPasscode = input.text.toString()
                if (enteredPasscode == DEVELOPER_PASSCODE) {
                    sharedPreferences.edit().putBoolean(KEY_DEVELOPER_MODE_ENABLED, true).apply()
                    Snackbar.make(binding.root, "Developer Mode Unlocked!", Snackbar.LENGTH_LONG).show()
                    showDeveloperOptions()
                } else {
                    Snackbar.make(binding.root, "Incorrect Passcode.", Snackbar.LENGTH_LONG).show()
                    findNavController().popBackStack() // Navigate back if passcode is wrong
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                findNavController().popBackStack() // Navigate back if cancelled
            }
            .setCancelable(false) // Prevent dismissing without action
            .show()
    }

    /**
     * Reveals the developer options UI (e.g., "Add Game" button).
     */
    private fun showDeveloperOptions() {
        binding.developerOptionsGroup.visibility = View.VISIBLE

        binding.buttonAddGame.setOnClickListener {
            findNavController().navigate(R.id.action_developerOptionsFragment_to_addGameFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
