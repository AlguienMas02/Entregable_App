package com.example.proyectofinal_itanestours.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectofinal_itanestours.database.TouristSpot
import com.example.proyectofinal_itanestours.databinding.ListItemSpotBinding // <-- Importante: esto se genera automáticamente

/**
 * Adaptador para el RecyclerView que muestra la lista de puntos turísticos.
 *
 * @param onFavoriteClicked Lambda que se invoca cuando el usuario pulsa el botón de favorito.
 * @param onItemClicked Lambda que se invoca cuando el usuario pulsa en cualquier parte de la tarjeta.
 */
class TouristSpotAdapter(
    private val onFavoriteClicked: (TouristSpot, Boolean) -> Unit,
    private val onItemClicked: (TouristSpot) -> Unit
) : ListAdapter<TouristSpot, TouristSpotAdapter.SpotViewHolder>(SpotDiffCallback) {

    /**
     * ViewHolder: Contiene las vistas para un solo ítem (list_item_spot.xml).
     */
    inner class SpotViewHolder(private val binding: ListItemSpotBinding) : RecyclerView.ViewHolder(binding.root) {

        // Vincula los datos del 'spot' a las vistas del layout
        fun bind(spot: TouristSpot, isFavorite: Boolean) {
            // 1. Asignar textos
            binding.textSpotName.text = spot.name
            binding.textSpotDescription.text = spot.description

            // 2. Cargar imagen con Glide
            Glide.with(binding.imageSpot.context) // Obtenemos el contexto desde la vista
                .load(spot.photoUrl) // La URL de la imagen
                .into(binding.imageSpot) // Dónde cargarla

            // 3. Establecer el estado del botón de favorito
            // Quitamos el listener temporalmente para que no se dispare al setear
            binding.buttonFavorite.setOnCheckedChangeListener(null)
            binding.buttonFavorite.isChecked = isFavorite

            // 4. Asignar los listeners (clicks)

            // Click en el ítem completo
            binding.root.setOnClickListener {
                onItemClicked(spot)
            }

            // Click en el botón de favorito
            binding.buttonFavorite.setOnCheckedChangeListener { _, isChecked ->
                onFavoriteClicked(spot, isChecked)
            }
        }
    }

    /**
     * Crea un nuevo ViewHolder (llama a esto cuando el RecyclerView necesita
     * una nueva "fila" de la lista).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        // "Infla" (crea) el layout XML y lo prepara para el ViewHolder
        val binding = ListItemSpotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SpotViewHolder(binding)
    }

    /**
     * Vincula los datos de un ítem a un ViewHolder (llama a esto para
     * rellenar una fila con datos).
     */
    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val spot = getItem(position)

        // --- Lógica para saber si este ítem es favorito ---
        // Esto es un truco, ya que el Adapter no conoce el ViewModel.
        // Hacemos que el ViewModel nos pase un set de IDs favoritos.
        // *Actualización:* ¡Esto lo manejaremos en el Fragment!
        // Por ahora, pasaremos el set de favoritos al adapter.

        // (Dejaremos esta lógica para el Fragment, que es quien tiene
        // acceso al ViewModel. Por ahora, asumimos que no es favorito)
        holder.bind(spot, false) // <-- Esto lo mejoraremos en el siguiente paso
    }

    // --- Necesitamos una forma de pasar la lista de favoritos al Adapter ---
    private var favoriteIds = emptySet<String>()

    fun setFavorites(ids: Set<String>) {
        favoriteIds = ids
        // Notificamos que los datos cambiaron para que vuelva a dibujar
        // los estados de los botones de favorito
        notifyDataSetChanged()
    }

    // Sobreescribimos onBindViewHolder para usar la lista de favoritos
    override fun onBindViewHolder(holder: SpotViewHolder, position: Int, payloads: MutableList<Any>) {
        val spot = getItem(position)
        val isFavorite = favoriteIds.contains(spot.spotId)
        holder.bind(spot, isFavorite)
    }

    /**
     * DiffUtil: Le dice al Adapter cómo calcular las diferencias
     * entre la lista vieja y la nueva.
     */
    companion object SpotDiffCallback : DiffUtil.ItemCallback<TouristSpot>() {
        override fun areItemsTheSame(oldItem: TouristSpot, newItem: TouristSpot): Boolean {
            // Comprueba si los ítems son el mismo (por ID)
            return oldItem.spotId == newItem.spotId
        }

        override fun areContentsTheSame(oldItem: TouristSpot, newItem: TouristSpot): Boolean {
            // Comprueba si el contenido del ítem ha cambiado (ej. nombre, foto)
            return oldItem == newItem
        }
    }
}