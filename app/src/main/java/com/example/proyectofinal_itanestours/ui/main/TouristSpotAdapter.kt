package com.example.proyectofinal_itanestours.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectofinal_itanestours.database.TouristSpot
import com.example.proyectofinal_itanestours.databinding.ListItemSpotBinding

class TouristSpotAdapter(
    private val onFavoriteClicked: (TouristSpot, Boolean) -> Unit,
    private val onItemClicked: (TouristSpot) -> Unit
) : ListAdapter<TouristSpot, TouristSpotAdapter.SpotViewHolder>(SpotDiffCallback) {

    /**
     * ViewHolder: Contiene las vistas para un solo ítem (list_item_spot.xml).
     */
    inner class SpotViewHolder(private val binding: ListItemSpotBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(spot: TouristSpot, isFavorite: Boolean) {
            binding.textSpotName.text = spot.name
            binding.textSpotDescription.text = spot.description

            Glide.with(binding.imageSpot.context)
                .load(spot.photoUrl)
                .into(binding.imageSpot)

            binding.buttonFavorite.setOnCheckedChangeListener(null)
            binding.buttonFavorite.isChecked = isFavorite

            binding.root.setOnClickListener {
                onItemClicked(spot)
            }

            binding.buttonFavorite.setOnCheckedChangeListener { _, isChecked ->
                onFavoriteClicked(spot, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val binding = ListItemSpotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SpotViewHolder(binding)
    }

    // --- ESTA ES LA FUNCIÓN CORREGIDA ---
    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val spot = getItem(position)
        // Obtenemos el estado de favorito usando la lista de IDs
        val isFavorite = favoriteIds.contains(spot.spotId)
        holder.bind(spot, isFavorite)
    }

    private var favoriteIds = emptySet<String>()

    fun setFavorites(ids: Set<String>) {
        favoriteIds = ids
        // Notificamos que los datos cambiaron para que vuelva a dibujar
        // los estados de los botones de favorito
        notifyDataSetChanged()
    }

    // --- HE ELIMINADO LA OTRA onBindViewHolder(..., payloads) PORQUE NO ERA NECESARIA ---

    companion object SpotDiffCallback : DiffUtil.ItemCallback<TouristSpot>() {
        override fun areItemsTheSame(oldItem: TouristSpot, newItem: TouristSpot): Boolean {
            return oldItem.spotId == newItem.spotId
        }

        override fun areContentsTheSame(oldItem: TouristSpot, newItem: TouristSpot): Boolean {
            return oldItem == newItem
        }
    }
}