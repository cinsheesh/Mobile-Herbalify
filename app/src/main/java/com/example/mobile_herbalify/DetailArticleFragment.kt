package com.example.mobile_herbalify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class DetailArticleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_article, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi UI (ID sudah disamakan dengan XML)
        val btnBack = view.findViewById<CardView>(R.id.btnBackArticle) // <-- Sudah diperbaiki!
        val btnFavorite = view.findViewById<CardView>(R.id.btnFavorite)
        val tvFavoriteIcon = view.findViewById<TextView>(R.id.tvFavoriteIcon)
        val ivImage = view.findViewById<ImageView>(R.id.ivReadArticleImage)
        val tvTitle = view.findViewById<TextView>(R.id.tvReadArticleTitle)
        val tvContent = view.findViewById<TextView>(R.id.tvReadArticleContent)

        // 2. Ambil Data
        val articleId = arguments?.getString("ARG_ID") ?: ""
        val userId = "user123"
        val db = FirebaseFirestore.getInstance()

        // 3. Cek Status Favorit
        if (articleId.isNotEmpty()) {
            db.collection("Favorites").document(userId + "_" + articleId).get().addOnSuccessListener { doc ->
                tvFavoriteIcon.text = if (doc.exists()) "❤️" else "🤍"
            }
        }

        // 4. Aksi Klik Favorit
        btnFavorite.setOnClickListener {
            if (articleId.isEmpty()) {
                Toast.makeText(context, "Gagal: ID Artikel kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val favRef = db.collection("Favorites").document(userId + "_" + articleId)
            favRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    favRef.delete().addOnSuccessListener {
                        tvFavoriteIcon.text = "🤍"
                        Toast.makeText(context, "Artikel dihapus dari favorit", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val data = mapOf("userId" to userId, "articleId" to articleId, "type" to "article")
                    favRef.set(data).addOnSuccessListener {
                        tvFavoriteIcon.text = "❤️"
                        Toast.makeText(context, "Artikel ditambahkan ke favorit!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 5. Aksi Klik Back
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 6. Tampilkan Teks & Gambar
        tvTitle.text = arguments?.getString("ARG_TITLE") ?: "Tanpa Judul"
        tvContent.text = arguments?.getString("ARG_CONTENT") ?: "Tidak ada konten."

        val imageUrl = arguments?.getString("ARG_IMAGE_URL") ?: ""
        if (imageUrl.isNotEmpty()) {
            Glide.with(requireContext()).load(imageUrl).into(ivImage)
        }
    }
}