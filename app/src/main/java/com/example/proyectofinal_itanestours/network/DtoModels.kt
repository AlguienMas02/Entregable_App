package com.example.proyectofinal_itanestours.network

data class TouristSpotDto (
    val spotId: String,
    val tourId: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val latitude: Double,
    val longitude: Double

)