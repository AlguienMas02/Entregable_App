package com.example.proyectofinal_itanestours.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("v1/006ddcea-cd9e-4563-8559-72d3246bdb9f")
    suspend fun getAllTouristSpots(): List<TouristSpotDto>

    @GET("v1/006ddcea-cd9e-4563-8559-72d3246bdb9f")
    suspend fun getSpotsForTour(@Query("tour") tourId: String): List<TouristSpotDto>
}