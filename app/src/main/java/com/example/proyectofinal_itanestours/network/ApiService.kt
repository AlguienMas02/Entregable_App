package com.example.proyectofinal_itanestours.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("v1/f8738d0d-7b9a-41eb-a412-6edc521e1184")
    suspend fun getAllTouristSpots(): List<TouristSpotDto>

    @GET("v1/f8738d0d-7b9a-41eb-a412-6edc521e1184")
    suspend fun getSpotsForTour(@Query("tour") tourId: String): List<TouristSpotDto>
}