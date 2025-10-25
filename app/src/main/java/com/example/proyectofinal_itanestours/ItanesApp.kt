package com.example.proyectofinal_itanestours

import android.app.Application
import com.example.proyectofinal_itanestours.database.AppDatabase
import com.example.proyectofinal_itanestours.network.RetrofitClient
import com.example.proyectofinal_itanestours.repository.TouristSpotRepository


class ItanesApp : Application() {

    // Creamos una instancia 'lazy' (perezosa) de la base de datos.
    // Solo se creará cuando se acceda a ella por primera vez.
    private val database by lazy { AppDatabase.getDatabase(this) }

    // Creamos el repositorio 'lazy'
    // Le pasamos el DAO de la base de datos y el ApiService de Retrofit.
    val repository by lazy {
        TouristSpotRepository(database, RetrofitClient.apiService)
    }
}