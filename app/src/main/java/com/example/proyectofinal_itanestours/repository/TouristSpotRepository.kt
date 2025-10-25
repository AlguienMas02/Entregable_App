package com.example.proyectofinal_itanestours.repository

import android.util.Log
import com.example.proyectofinal_itanestours.database.AppDatabase
import com.example.proyectofinal_itanestours.database.Favorite
import com.example.proyectofinal_itanestours.database.TouristSpot
import com.example.proyectofinal_itanestours.network.ApiService
import com.example.proyectofinal_itanestours.network.RetrofitClient
import com.example.proyectofinal_itanestours.network.TouristSpotDto
import kotlinx.coroutines.flow.Flow

class TouristSpotRepository(
    private val database: AppDatabase,
    private val apiService: ApiService
) {


    fun getSpotsByTour(tourId: String): Flow<List<TouristSpot>> {
        return database.touristSpotDao().getSpotsByTour(tourId)
    }

    fun getSpotById(spotId: String): Flow<TouristSpot> {
        return database.touristSpotDao().getSpotById(spotId)
    }

    fun getFavoriteSpotIds(): Flow<List<String>> {
        return database.touristSpotDao().getFavoriteSpotIds()
    }


    suspend fun refreshSpots() {
        try {
            val remoteSpots = apiService.getAllTouristSpots()

            val localSpots = remoteSpots.map { it.toDatabaseEntity() }

            database.touristSpotDao().insertAllSpots(localSpots)

            Log.d("Repository", "Datos sincronizados correctamente.")

        } catch (e: Exception) {
            Log.e("Repository", "Error al sincronizar datos: ${e.message}")
        }
    }
    /**
     * Añade un nuevo favorito a la base de datos.
     * Llama directamente al DAO.
     */
    suspend fun addFavorite(favorite: Favorite) {
        database.touristSpotDao().addFavorite(favorite)
    }

    /**
     * Elimina un favorito de la base de datos.
     * Llama directamente al DAO.
     */
    suspend fun removeFavorite(spotId: String) {
        database.touristSpotDao().removeFavorite(spotId)
    }
}


private fun TouristSpotDto.toDatabaseEntity(): TouristSpot {
    return TouristSpot(
        spotId = this.spotId,
        tourId = this.tourId,
        name = this.name,
        description = this.description,
        photoUrl = this.photoUrl,
        latitude = this.latitude,
        longitude = this.longitude
    )
}