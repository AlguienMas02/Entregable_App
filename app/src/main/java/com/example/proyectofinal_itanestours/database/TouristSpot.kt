package com.example.proyectofinal_itanestours.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tourist_spots")
data class TouristSpot(
    @PrimaryKey(autoGenerate = true)
    val spotId: String,
    val tourId: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val latitude: Double,
    val longitude: Double


)