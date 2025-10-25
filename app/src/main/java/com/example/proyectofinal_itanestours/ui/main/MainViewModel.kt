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
import kotlinx.coroutines.launch

// Definimos un estado para la UI, que contendrá la lista de spots
// y la información de cuáles son favoritos.
data class UiState(
    val spots: List<TouristSpot> = emptyList(),
    val favoriteSpotIds: Set<String> = emptySet()
)

class MainViewModel(private val repository: TouristSpotRepository) : ViewModel() {

    // --- ID del Tour que estamos viendo ---
    // Usaremos el de "lima_centro" que creamos en la API de Mocki
    private val tourId = "lima_centro"

    // --- Flujo de datos desde el Repositorio ---
    // Obtenemos el Flow de los Puntos Turísticos
    private val spotsFlow = repository.getSpotsByTour(tourId)

    // Obtenemos el Flow de los IDs Favoritos
    private val favoritesFlow = repository.getFavoriteSpotIds()

    /**
     * Exponemos el estado de la UI (UiState).
     * Usamos 'combine' para mezclar los datos de spotsFlow y favoritesFlow.
     * Cada vez que cualquiera de los dos cambie, se emitirá un nuevo UiState.
     */
    val uiState: StateFlow<UiState> = combine(spotsFlow, favoritesFlow) { spots, favIds ->
        UiState(spots, favIds.toSet())
    }.stateIn( // Convertimos el Flow en un StateFlow
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Mantiene el flow activo 5s
        initialValue = UiState() // Valor inicial mientras carga
    )

    // --- Acciones del Usuario ---

    /**
     * Llama al Repositorio para que actualice los datos desde la API.
     * Se debe llamar al iniciar la app o en un "pull-to-refresh".
     */
    fun refreshData() {
        // Lanzamos una corutina en el 'viewModelScope'
        // Este 'scope' se cancela automáticamente cuando el ViewModel se destruye.
        viewModelScope.launch {
            repository.refreshSpots()
        }
    }

    /**
     * Marca o desmarca un punto como favorito.
     */
    fun toggleFavorite(spotId: String) {
        viewModelScope.launch {
            if (uiState.value.favoriteSpotIds.contains(spotId)) {
                // Ya es favorito, lo quitamos
                repository.removeFavorite(spotId)
            } else {
                // No es favorito, lo añadimos
                repository.addFavorite(Favorite(spotId = spotId))
            }
        }
    }
}