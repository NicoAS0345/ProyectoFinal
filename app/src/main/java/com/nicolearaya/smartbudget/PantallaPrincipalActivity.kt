package com.nicolearaya.smartbudget

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nicolearaya.smartbudget.databinding.ActivityPantallaPrincipalBinding
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI // Asegúrate de tener este import
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavController
import androidx.navigation.Navigation


@AndroidEntryPoint
class PantallaPrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPantallaPrincipalBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura el NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        setupBottomNav()


        // Configuración del botón flotante
        binding.anadirGasto.setOnClickListener {
            // Navegar al fragmento de añadir gasto
            if (navController.currentDestination?.id == R.id.nav_home) {
                navController.navigate(R.id.action_to_add_expense)
            }
        }

        setupNavigation()

    }

    // Método para que los fragmentos controlen la visibilidad del FAB
    fun showHideFab(show: Boolean) {
        binding.anadirGasto.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupBottomNav() {
        binding.barraNavegacion.setOnItemSelectedListener { item ->
            Log.d("NAV_DEBUG", "Intentando navegar al ID: ${item.itemId}")
            when (item.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.nav_home)
                    true
                }
                R.id.historyFragment -> {
                    navController.navigate(R.id.historyFragment)
                    true
                }
                R.id.budgetFragment -> {
                    navController.navigate(R.id.budgetFragment)
                    true
                }
                else -> false
            }
        }


        // Configurar el botón flotante
        binding.anadirGasto.setOnClickListener {
            navController.navigate(R.id.addExpenseFragment)
        }

        // Ocultar FAB en pantallas que no son el home
        // En PantallaPrincipalActivity.kt
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    binding.barraNavegacion.visibility = View.GONE
                    binding.anadirGasto.visibility = View.GONE
                }
                else -> {
                    binding.barraNavegacion.visibility = View.VISIBLE
                    binding.anadirGasto.visibility =
                        if (destination.id == R.id.nav_home) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}