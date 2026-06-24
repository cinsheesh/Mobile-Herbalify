package com.example.mobile_herbalify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_herbalify.R
import com.example.mobile_herbalify.model.Plant
import com.bumptech.glide.Glide

class AdminPlantAdapter(
    private val plantList: List<Plant>,
    private val onEditClick: (Plant) -> Unit,   // Fungsi untuk tombol Edit
    private val onDeleteClick: (Plant) -> Unit  // Fungsi untuk tombol Delete
) : RecyclerView.Adapter<AdminPlantAdapter.AdminViewHolder>() {

    class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivAdminPlantIcon)
        val tvName: TextView = itemView.findViewById(R.id.tvAdminPlantName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvAdminPlantDesc)
        val btnEdit: CardView = itemView.findViewById(R.id.btnEditPlant)
        val btnDelete: CardView = itemView.findViewById(R.id.btnDeletePlant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_plant, parent, false)
        return AdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val plant = plantList[position]

        // Pasang data teks ke UI
        holder.tvName.text = plant.name
        holder.tvDesc.text = "${plant.latinName} · ${plant.category}"

        // Pasang gambar default (nanti bisa diganti pakai Glide/Picasso kalau ambil URL dari Firebase)
        if (plant.imageUrl.isNotEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(plant.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivIcon)
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Beri aksi saat Admin klik tombol Edit (Pensil)
        holder.btnEdit.setOnClickListener {
            onEditClick(plant)
        }

        // Beri aksi saat Admin klik tombol Delete (Sampah)
        holder.btnDelete.setOnClickListener {
            onDeleteClick(plant)
        }
    }

    override fun getItemCount(): Int = plantList.size
}