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

    // --- CÓDIGO SIMPLIFICADO ---
    // Directamente exponemos el Flow del repositorio.
    // Este Flow emitirá null si no encuentra el spot, y luego
    // emitirá el TouristSpot cuando esté disponible en la BD.
    val spot: StateFlow<TouristSpot?> = repository.getSpotById(spotId)
        .flowOn(Dispatchers.IO) // Corre la consulta en un hilo de fondo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // El valor inicial es null
        )
    // --- FIN CÓDIGO SIMPLIFICADO ---
}

// La Factory no cambia
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