package com.example.proyectofinal_itanestours.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.proyectofinal_itanestours.ItanesApp
import com.example.proyectofinal_itanestours.database.TouristSpot
import com.example.proyectofinal_itanestours.databinding.FragmentSpotDetailBinding
import com.example.proyectofinal_itanestours.ui.main.MainViewModel
import com.example.proyectofinal_itanestours.ui.main.MainViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.navigation.fragment.navArgs

class SpotDetailFragment : Fragment() {

    private var _binding: FragmentSpotDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView

    // Argumentos de navegación (para obtener el spotId)
    private val args: SpotDetailFragmentArgs by navArgs()

    // ViewModel para ESTE fragmento (para cargar el Spot)
    private val detailViewModel: SpotDetailViewModel by viewModels {
        SpotDetailViewModelFactory(
            (requireActivity().application as ItanesApp).repository,
            args.spotId // Pasamos el ID del argumento
        )
    }

    // ViewModel de la Actividad (para manejar favoritos)
    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModelFactory(
            (requireActivity().application as ItanesApp).repository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSpotDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView
        setupMapDefaults()

        observeSpotDetails()
        observeFavoriteStatus()
    }

    private fun observeSpotDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observa el spot específico
                detailViewModel.spot.collectLatest { spot ->
                    if (spot != null) {
                        bindSpotData(spot)
                        setupMapLocation(spot)
                    }
                }
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observa la lista de favoritos del MainViewModel
                mainViewModel.uiState.collectLatest { state ->
                    val isFavorite = state.favoriteSpotIds.contains(args.spotId)
                    binding.buttonDetailFavorite.setOnCheckedChangeListener(null)
                    binding.buttonDetailFavorite.isChecked = isFavorite

                    // Re-asignamos el listener
                    binding.buttonDetailFavorite.setOnCheckedChangeListener { _, _ ->
                        mainViewModel.toggleFavorite(args.spotId)
                    }
                }
            }
        }
    }

    // Rellena la UI con los datos del Spot
    private fun bindSpotData(spot: TouristSpot) {
        binding.textDetailName.text = spot.name
        binding.textDetailDescription.text = spot.description

        Glide.with(this)
            .load(spot.photoUrl)
            .into(binding.imageDetailSpot)
    }

    // Configuración inicial del mapa
    private fun setupMapDefaults() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)
    }

    // Centra el mapa y añade el marcador
    private fun setupMapLocation(spot: TouristSpot) {
        val spotLocation = GeoPoint(spot.latitude, spot.longitude)

        // Centra el mapa
        mapView.controller.setCenter(spotLocation)

        // Limpia marcadores anteriores
        mapView.overlays.clear()

        // Crea y añade el marcador
        val marker = Marker(mapView)
        marker.position = spotLocation
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = spot.name
        mapView.overlays.add(marker)

        // Refresca el mapa
        mapView.invalidate()
    }

    // --- Manejo del ciclo de vida del Mapa ---
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
        _binding = null
    }
}