package com.novah.mathmind.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.novah.mathmind.NovahMathMindApplication
import com.novah.mathmind.R
import com.novah.mathmind.data.Game
import com.novah.mathmind.data.Game.BuiltInGame
import com.novah.mathmind.data.Game.CustomGameItem
import com.novah.mathmind.databinding.FragmentHomeBinding
import com.novah.mathmind.ui.MainViewModel
import com.novah.mathmind.ui.MainViewModelFactory
import com.novah.mathmind.ui.WebViewGameActivity

/**
 * HomeFragment displays a grid of available games.
 * Navigates to appropriate game activities or fragments based on game type.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((activity?.application as NovahMathMindApplication).repository)
    }

    private lateinit var gameAdapter: GameAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupRecyclerView()
        observeGames()
    }

    /**
     * Sets up the options menu for the fragment (e.g., Settings icon).
     */
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Configures the RecyclerView to display game items in a grid layout.
     */
    private fun setupRecyclerView() {
        gameAdapter = GameAdapter { game ->
            when (game) {
                is BuiltInGame -> {
                    // Launch built-in game activity
                    val intent = Intent(requireContext(), game.activityClass)
                    startActivity(intent)
                }
                is CustomGameItem -> {
                    // Launch WebView game activity
                    val intent = Intent(requireContext(), WebViewGameActivity::class.java).apply {
                        putExtra(WebViewGameActivity.EXTRA_CUSTOM_GAME_ID, game.id.toLong())
                    }
                    startActivity(intent)
                }
            }
        }
        binding.recyclerViewGames.apply {
            layoutManager = GridLayoutManager(context, 2) // 2 columns for grid layout
            adapter = gameAdapter
        }
    }

    /**
     * Observes the list of games from the ViewModel and updates the RecyclerView.
     */
    private fun observeGames() {
        viewModel.allGames.observe(viewLifecycleOwner) { games ->
            gameAdapter.submitList(games)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
