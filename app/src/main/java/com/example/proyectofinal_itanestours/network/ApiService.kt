package com.example.proyectofinal_itanestours.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("v1/9e786147-1601-4a0f-b7be-8b428fd66cda")
    suspend fun getAllTouristSpots(): List<TouristSpotDto>

    @GET("v1/9e786147-1601-4a0f-b7be-8b428fd66cda")
    suspend fun getSpotsForTour(@Query("tour") tourId: String): List<TouristSpotDto>
}