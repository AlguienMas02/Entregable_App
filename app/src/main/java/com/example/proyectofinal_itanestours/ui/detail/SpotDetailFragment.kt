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
import androidx.navigation.fragment.navArgs
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.Marker
import android.content.ActivityNotFoundException
import android.util.Log
import android.widget.Toast


class SpotDetailFragment : Fragment() {

    private var _binding: FragmentSpotDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView

    private lateinit var locationOverlay: MyLocationNewOverlay
    private var currentSpot: TouristSpot? = null

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

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permiso concedido, activamos la capa de ubicación
                enableUserLocation()
            } else {
                // Permiso denegado, puedes mostrar un Toast si quieres
            }
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

        setupLocationOverlay()
        checkLocationPermission()
        setupDirectionsButton()


        observeSpotDetails()
        observeFavoriteStatus()
    }

    private fun observeSpotDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observa el spot (que puede ser nulo)
                detailViewModel.spot.collectLatest { spot ->
                    // Actualiza la variable currentSpot CADA VEZ que el Flow emite
                    currentSpot = spot

                    if (spot != null) {
                        // Si tenemos datos, actualiza la UI y activa el botón
                        bindSpotData(spot)
                        setupMapLocation(spot)
                        binding.buttonGetDirections.isEnabled = true
                        Log.d("SpotDetailFragment", "Spot cargado: ${spot.name}") // Log para confirmar
                    } else {
                        // Si es nulo (estado inicial o no encontrado),
                        // MANTÉN el botón desactivado.
                        binding.buttonGetDirections.isEnabled = false
                        Log.d("SpotDetailFragment", "Spot es null (esperando datos...)") // Log para confirmar
                        // Opcional: podrías mostrar un ProgressBar aquí
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


    private fun setupDirectionsButton() {
        binding.buttonGetDirections.setOnClickListener {
            Log.d("SpotDetailFragment", "Botón 'Cómo llegar' presionado.")

            // --- ¡LA COMPROBACIÓN MÁS IMPORTANTE VA PRIMERO! ---
            if (currentSpot == null) {
                Log.e("SpotDetailFragment", "El Spot no se ha cargado todavía (clic rápido).")
                Toast.makeText(
                    context,
                    "Datos aún cargando, espera un segundo.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener // Detiene la ejecución aquí si no hay datos
            }
            // --- FIN DE LA COMPROBACIÓN ---

            Log.d(
                "SpotDetailFragment",
                "currentSpot no es nulo."
            ) // Este log ahora debería aparecer siempre

            val spot = currentSpot!! // Ahora es seguro usar !!
            val spotLat = spot.latitude
            val spotLng = spot.longitude
            Log.d("SpotDetailFragment", "Coordenadas: Lat=$spotLat, Lng=$spotLng")

            try {
                Log.d("SpotDetailFragment", "Intentando Intent a Google Maps (Navegación)...")
                val gmmIntentUri = Uri.parse("google.navigation:q=$spotLat,$spotLng&mode=d")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                    Log.d("SpotDetailFragment", "Google Maps encontrado. Lanzando Intent...")
                    startActivity(mapIntent)
                    Log.d("SpotDetailFragment", "Intent a Google Maps lanzado.")
                } else {
                    Log.d(
                        "SpotDetailFragment",
                        "Google Maps NO encontrado. Intentando navegador..."
                    )
                    // URL Corregida para trazar ruta en web
                    val browserIntentUri = Uri.parse(
                        "https://www.google.com/maps/dir/?api=1&destination=$($spotLat),$spotLng"
                    )
                    val browserIntent = Intent(Intent.ACTION_VIEW, browserIntentUri)
                    startActivity(browserIntent)
                    Log.d("SpotDetailFragment", "Intent a navegador lanzado.")
                }
            } catch (e: ActivityNotFoundException) {
                Log.e(
                    "SpotDetailFragment",
                    "ActivityNotFoundException: No se pudo abrir ningún mapa.",
                    e
                )
                Toast.makeText(context, "No se encontró una app de mapas.", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                Log.e("SpotDetailFragment", "Excepción inesperada al lanzar Intent.", e)
                Toast.makeText(context, "Error al intentar abrir mapas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- AÑADE ESTAS FUNCIONES PARA LA UBICACIÓN DEL USUARIO ---
    private fun setupLocationOverlay() {
        val provider = GpsMyLocationProvider(context)
        locationOverlay = MyLocationNewOverlay(provider, mapView)
        mapView.overlays.add(locationOverlay)
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Ya tienes permiso
                enableUserLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // (Opcional) Mostrar un diálogo explicando por qué necesitas el permiso
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                // Pide el permiso
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun enableUserLocation() {
        locationOverlay.enableMyLocation()
        // Opcional: Centrar el mapa en la ubicación del usuario al inicio
        // locationOverlay.runOnFirstFix {
        //     if (isAdded) { // Comprueba que el fragmento siga "vivo"
        //         mapView.controller.animateTo(locationOverlay.myLocation)
        //     }
        // }
    }

    // --- MODIFICA onResume y onPause ---
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        locationOverlay.enableMyLocation() // <-- AÑADE ESTO
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        locationOverlay.disableMyLocation() // <-- AÑADE ESTO
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
        _binding = null
    }
}
