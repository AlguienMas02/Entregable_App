package com.example.proyectofinal_itanestours.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinal_itanestours.ItanesApp
import com.example.proyectofinal_itanestours.databinding.FragmentFavoritesBinding
import com.example.proyectofinal_itanestours.ui.main.MainViewModel
import com.example.proyectofinal_itanestours.ui.main.MainViewModelFactory
import com.example.proyectofinal_itanestours.ui.main.TouristSpotAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    // Usamos el MainViewModel compartido
    private val viewModel: MainViewModel by activityViewModels {
        MainViewModelFactory(
            (requireActivity().application as ItanesApp).repository
        )
    }

    private lateinit var favoritesAdapter: TouristSpotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeFavoritesState()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = TouristSpotAdapter(
            onFavoriteClicked = { spot, _ ->
                viewModel.toggleFavorite(spot.spotId)
                // En favoritos, al quitarlo, debería desaparecer de la lista
            },
            onItemClicked = { spot ->
                // Navegar al detalle desde Favoritos
                val action =
                    FavoritesFragmentDirections.actionFavoritesFragmentToSpotDetailFragment(spot.spotId)
                findNavController().navigate(action)
            }
        )

        binding.recyclerViewFavorites.apply {
            adapter = favoritesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeFavoritesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observamos el StateFlow de favoritos que crearemos en el ViewModel
                viewModel.favoritesUiState.collectLatest { state ->
                    favoritesAdapter.submitList(state.spots)
                    favoritesAdapter.setFavorites(state.favoriteSpotIds) // Para pintar bien la estrella

                    // Mostrar/Ocultar mensaje de "no hay favoritos"
                    binding.textNoFavorites.isVisible = state.spots.isEmpty()
                    binding.recyclerViewFavorites.isVisible = state.spots.isNotEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}