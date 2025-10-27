package com.example.proyectofinal_itanestours.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal_itanestours.database.Favorite
import com.example.proyectofinal_itanestours.database.TouristSpot
import com.example.proyectofinal_itanestours.repository.TouristSpotRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

// Definimos un estado para la UI, que contendrá la lista de spots
// y la información de cuáles son favoritos.
data class SpotsUiState( // <-- Renombramos de UiState a SpotsUiState
    val spots: List<TouristSpot> = emptyList(),
    val favoriteSpotIds: Set<String> = emptySet()
)

class MainViewModel(private val repository: TouristSpotRepository) : ViewModel() {

    private val tourId = "lima_centro" // O el ID que estés usando

    // Flujo de TODOS los Puntos Turísticos para el tour actual
    private val allSpotsFlow = repository.getSpotsByTour(tourId).flowOn(Dispatchers.IO)

    // Flujo de los IDs Favoritos
    private val favoriteIdsFlow = repository.getFavoriteSpotIds().flowOn(Dispatchers.IO)

    /**
     * StateFlow para la pantalla principal (TourListFragment).
     * Combina todos los spots con los IDs favoritos.
     */
    val uiState: StateFlow<SpotsUiState> = combine(allSpotsFlow, favoriteIdsFlow) { spots, favIds ->
        SpotsUiState(spots, favIds.toSet())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpotsUiState()
    )

    /**
     * StateFlow NUEVO para la pantalla de Favoritos (FavoritesFragment).
     * Filtra la lista de 'allSpotsFlow' usando 'favoriteIdsFlow'.
     */
    val favoritesUiState: StateFlow<SpotsUiState> = combine(allSpotsFlow, favoriteIdsFlow) { spots, favIds ->
        val favoriteSpots = spots.filter { spot -> favIds.contains(spot.spotId) }
        SpotsUiState(favoriteSpots, favIds.toSet()) // Pasamos los spots filtrados y el set completo de IDs
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpotsUiState() // Estado inicial vacío
    )


    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.refreshSpots()
        }
    }

    fun toggleFavorite(spotId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Usamos el Set del uiState principal para saber el estado actual
            if (uiState.value.favoriteSpotIds.contains(spotId)) {
                repository.removeFavorite(spotId)
            } else {
                repository.addFavorite(Favorite(spotId = spotId))
            }
        }
    }
}