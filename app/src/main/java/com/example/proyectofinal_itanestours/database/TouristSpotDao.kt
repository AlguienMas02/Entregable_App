package com.example.proyectofinal_itanestours.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TouristSpotDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSpots(spots: List<TouristSpot>)

    @Query("SELECT * FROM tourist_spots WHERE tourId = :tourId ORDER BY name ASC")
    fun getSpotsByTour(tourId: String): Flow<List<TouristSpot>>

    @Query("SELECT * FROM tourist_spots WHERE spotId = :spotId")
    fun getSpotById(spotId: String): Flow<TouristSpot>

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Ignora si ya es favorito
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE spotId = :spotId")
    suspend fun removeFavorite(spotId: String)


    @Query("SELECT spotId FROM favorites")
    fun getFavoriteSpotIds(): Flow<List<String>>
}