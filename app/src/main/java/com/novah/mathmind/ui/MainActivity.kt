package com.novah.mathmind.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.novah.mathmind.R
import com.novah.mathmind.databinding.ActivityMainBinding

/**
 * Main Activity of the NovahMathMind application.
 * Hosts the Navigation Component's NavHostFragment.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Navigation Component
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configure AppBarConfiguration to define top-level destinations (no Up button shown)
        // For fragments not explicitly in appBarConfiguration, the Up button will automatically appear.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment, R.id.settingsFragment, R.id.developerOptionsFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Handles the Up button navigation in the action bar.
     * Delegates to the NavController.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
