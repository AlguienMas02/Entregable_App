package com.example.proyectofinal_itanestours.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = TouristSpot::class,
            parentColumns = ["spotId"],
            childColumns = ["spotId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val favoriteId: Int = 0,
    val spotId: String
)