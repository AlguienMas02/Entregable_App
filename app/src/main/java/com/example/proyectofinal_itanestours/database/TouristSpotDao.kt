package com.example.proyectofinal_itanestours.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSpots(spots: List<TouristSpot>)

    @Query("SELECT * FROM tourist_spots WHERE tourId = :tourId ORDER BY name ASC")
    fun getSpotsByTour(tourId: String): Flow<List<TouristSpot>>

    @Query("SELECT * FROM tourist_spots WHERE spotId = :spotId")
    suspend fun getSpotById(spotId: String): Flow<TouristSpot>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE spotId = :spotId")
    suspend fun removeFavorite(spotId: String)

    @Query("SELECT * FROM favorites")
    fun getFavoritesSpotIds(): Flow<List<String>>
}
