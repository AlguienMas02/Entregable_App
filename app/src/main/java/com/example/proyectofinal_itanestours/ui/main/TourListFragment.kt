package com.example.proyectofinal_itanestours.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinal_itanestours.ItanesApp
import com.example.proyectofinal_itanestours.databinding.FragmentTourListBinding // <-- Importa el ViewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TourListFragment : Fragment() {

    // 1. Configurar View Binding
    private var _binding: FragmentTourListBinding? = null
    private val binding get() = _binding!!

    // 2. Inicializar el ViewModel
    // Usamos la Factory que creamos para inyectar el Repositorio
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            (requireActivity().application as ItanesApp).repository
        )
    }

    // 3. Declarar el Adaptador
    private lateinit var spotAdapter: TouristSpotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout usando View Binding
        _binding = FragmentTourListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(View: View, savedInstanceState: Bundle?) {
        super.onViewCreated(View, savedInstanceState)

        // 4. Configurar el Adaptador y el RecyclerView
        setupRecyclerView()

        // 5. Configurar el "Tirar para refrescar" (SwipeRefresh)
        setupSwipeRefresh()

        // 6. Observar los datos del ViewModel
        observeUiState()

        // 7. Pedir la carga inicial de datos
        // (Solo si la lista está vacía, para no recargar al rotar)
        if (viewModel.uiState.value.spots.isEmpty()) {
            viewModel.refreshData()
        }
    }

    private fun setupRecyclerView() {
        // Inicializa el adaptador con las acciones de click
        spotAdapter = TouristSpotAdapter(
            onFavoriteClicked = { spot, _ ->
                // Acción 1: El usuario pulsó la estrella
                viewModel.toggleFavorite(spot.spotId)
            },
            onItemClicked = { spot ->
                // Acción 2: El usuario pulsó la tarjeta
                // TODO: Navegar a la pantalla de detalle
                Toast.makeText(context, "Ver detalle de ${spot.name}", Toast.LENGTH_SHORT).show()
            }
        )

        // Asigna el adaptador y el layout manager al RecyclerView
        binding.recyclerViewSpots.apply {
            adapter = spotAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupSwipeRefresh() {
        // Cuando el usuario "tira hacia abajo", llama a refreshData()
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()
            // El 'isRefreshing' se pondrá en 'false' desde el observer
        }
    }

    private fun observeUiState() {
        // Usamos 'viewLifecycleOwner' para que la corutina
        // se cancele automáticamente cuando el fragmento se destruya
        viewLifecycleOwner.lifecycleScope.launch {
            // 'repeatOnLifecycle(STARTED)' asegura que solo
            // colectamos datos cuando el fragmento está visible
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Empezamos a escuchar el StateFlow del ViewModel
                viewModel.uiState.collectLatest { state ->
                    // 1. Actualizar la lista en el adaptador
                    spotAdapter.submitList(state.spots)

                    // 2. Actualizar los favoritos en el adaptador
                    // (Esto es clave para que los botones (★) se pinten bien)
                    spotAdapter.setFavorites(state.favoriteSpotIds)

                    // 3. Detener la animación de "refrescar"
                    // (Lo hacemos aquí para que se detenga cuando los datos *lleguen*)
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpia la referencia al binding para evitar memory leaks
        _binding = null
    }
}