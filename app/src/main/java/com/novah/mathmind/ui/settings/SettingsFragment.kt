package com.novah.mathmind.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.novah.mathmind.R
import com.novah.mathmind.ui.developer.DeveloperOptionsFragment

/**
 * Settings Fragment for the application.
 * Contains the "Coded by Novah" easter egg for developer mode.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var sharedPreferences: SharedPreferences
    private var tapCount = 0
    private var lastTapTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private val resetTapCountRunnable = Runnable {
        tapCount = 0
        lastTapTime = 0
    }

    companion object {
        private const val PREFS_NAME = "NovahMathMindPrefs"
        private const val KEY_DEVELOPER_MODE_UNLOCKED = "developer_mode_unlocked"
        private const val TAP_COUNT_THRESHOLD = 5
        private const val TAP_TIME_WINDOW_MS = 1000L // 1 second
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_root_preferences, rootKey)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        // Find the custom preference layout that contains "Coded by Novah" text
        val codedByNovahPreference = findPreference<Preference>("coded_by_novah")
        codedByNovahPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            handleEasterEggTap()
            true
        }
        return view
    }

    /**
     * Handles taps on the "Coded by Novah" preference for the Easter Egg.
     * If 5 taps occur within a short time window, Developer Mode is unlocked.
     */
    private fun handleEasterEggTap() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTapTime < TAP_TIME_WINDOW_MS) {
            tapCount++
            if (tapCount >= TAP_COUNT_THRESHOLD) {
                // Easter Egg triggered!
                unlockDeveloperMode()
                tapCount = 0 // Reset after triggering
            }
        } else {
            // Reset if tap too slow
            tapCount = 1
        }
        lastTapTime = currentTime

        // Ensure the reset runnable is not duplicated and schedule a reset
        handler.removeCallbacks(resetTapCountRunnable)
        handler.postDelayed(resetTapCountRunnable, TAP_TIME_WINDOW_MS)
    }

    /**
     * Unlocks developer mode and navigates to the Developer Options fragment.
     */
    private fun unlockDeveloperMode() {
        val isDeveloperModeUnlocked = sharedPreferences.getBoolean(KEY_DEVELOPER_MODE_UNLOCKED, false)
        if (!isDeveloperModeUnlocked) {
            sharedPreferences.edit().putBoolean(KEY_DEVELOPER_MODE_UNLOCKED, true).apply()
            Snackbar.make(
                requireView(),
                "Developer Mode Unlocked! Check for Developer Options.",
                Snackbar.LENGTH_LONG
            ).show()
        }
        // Navigate to developer options fragment (authentication happens there)
        findNavController().navigate(R.id.action_settingsFragment_to_developerOptionsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(resetTapCountRunnable) // Prevent memory leaks
    }
}
