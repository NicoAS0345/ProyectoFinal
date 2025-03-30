package com.nicolearaya.smartbudget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nicolearaya.smartbudget.databinding.ActivityPantallaPrincipalBinding
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.NavOptions
import androidx.navigation.ui.NavigationUI // Asegúrate de tener este import


class PantallaPrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPantallaPrincipalBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la barra de navegación inferior
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Define los destinos principales
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                /*R.id.nav_search,
                R.id.nav_settings*/
            )

        )
        // Configura barra de navegación inferior
        binding.barraNavegacion.setupWithNavController(navController)

        // Configuración del botón flotante
        binding.anadirGasto.setOnClickListener {
            // Navegar al fragmento de añadir gasto
            if (navController.currentDestination?.id == R.id.nav_home) {
                navController.navigate(R.id.action_to_add_expense)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()

    }
}