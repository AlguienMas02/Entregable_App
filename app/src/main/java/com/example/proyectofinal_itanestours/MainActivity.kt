package com.example.proyectofinal_itanestours

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController // <-- Importa esto
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration // <-- Importa esto
import androidx.navigation.ui.setupActionBarWithNavController // <-- Importa esto
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar // <-- Importa esto
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtener NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configurar Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar) // Establece la Toolbar como ActionBar

        // Definir destinos de nivel superior (los que están en BottomNav)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.tourListFragment, R.id.favoritesFragment // IDs de tu nav_graph.xml
            )
        )

        // Conectar ActionBar (Toolbar) con NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Configurar BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)
    }

    // --- AÑADE ESTA FUNCIÓN para manejar el botón "Atrás" de la Toolbar ---
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    // --- FIN ---
}