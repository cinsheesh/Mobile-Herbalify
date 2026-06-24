package com.example.mobile_herbalify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobile_herbalify.R
import com.example.mobile_herbalify.model.Plant

class PopularPlantAdapter(
    private val plantList: List<Plant>,
    private val onClick: (Plant) -> Unit
) : RecyclerView.Adapter<PopularPlantAdapter.PlantViewHolder>() {

    class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPlantImage: ImageView = itemView.findViewById(R.id.ivPlantImage)
        val tvPlantName: TextView = itemView.findViewById(R.id.tvPlantName)
        val tvLatinName: TextView = itemView.findViewById(R.id.tvLatinName)
        val tvPlantCategory: TextView = itemView.findViewById(R.id.tvPlantCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plantList[position]

        holder.tvPlantName.text = plant.name
        holder.tvLatinName.text = plant.latinName
        holder.tvPlantCategory.text = plant.category

        // LOGIKA GAMBAR YANG SUDAH DIGABUNG DAN BERSIH DARI DUPLIKAT
        if (plant.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(plant.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image) // Ikon error bawaan
                .skipMemoryCache(true) // Memaksa ambil gambar baru
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // Tidak simpan di disk
                .into(holder.ivPlantImage)
        } else {
            // Jika kosong, pakai gambar local resId bawaan
            holder.ivPlantImage.setImageResource(plant.imageResId)
        }

        holder.itemView.setOnClickListener {
            onClick(plant)
        }
    }

    override fun getItemCount(): Int = plantList.size
}