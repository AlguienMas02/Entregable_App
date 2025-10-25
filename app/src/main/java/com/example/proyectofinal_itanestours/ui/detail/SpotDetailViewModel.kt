package com.example.proyectofinal_itanestours.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal_itanestours.database.TouristSpot
import com.example.proyectofinal_itanestours.repository.TouristSpotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class SpotDetailViewModel(
    repository: TouristSpotRepository,
    spotId: String
) : ViewModel() {

    // Exponemos un Flow que observa solo el Spot solicitado
    val spot: StateFlow<TouristSpot?> = repository.getSpotById(spotId)
        .flowOn(Dispatchers.IO) // Corre la consulta en un hilo de fondo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // Valor inicial nulo mientras carga
        )
}

/**
 * Factory para poder pasar el Repositorio y el spotId al ViewModel
 */
class SpotDetailViewModelFactory(
    private val repository: TouristSpotRepository,
    private val spotId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpotDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpotDetailViewModel(repository, spotId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}