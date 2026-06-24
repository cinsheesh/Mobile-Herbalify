package com.example.mobile_herbalify

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class DetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi ID Komponen UI
        val btnBack = view.findViewById<CardView>(R.id.btnBack)
        val btnFavorite = view.findViewById<CardView>(R.id.btnFavorite)
        val tvFavoriteIcon = view.findViewById<TextView>(R.id.tvFavoriteIcon)
        val tvDetailName = view.findViewById<TextView>(R.id.tvDetailName)
        val tvLatinName = view.findViewById<TextView>(R.id.tvLatinName)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvUsage = view.findViewById<TextView>(R.id.tvUsage)
        val tvWarning = view.findViewById<TextView>(R.id.tvWarning)
        val ivDetailImage = view.findViewById<ImageView>(R.id.ivDetailImage)
        val llBenefitContainer = view.findViewById<LinearLayout>(R.id.llBenefitContainer)
        val llProcessingContainer = view.findViewById<LinearLayout>(R.id.llProcessingContainer)

        // Ambil ID Tanaman yang dikirim dari HomeFragment
        val plantId = arguments?.getString("ARG_ID") ?: ""
        val userId = "user123"
        val db = FirebaseFirestore.getInstance()

        // 2. Cek Status Favorit Saat Halaman Terbuka
        if (plantId.isNotEmpty()) {
            db.collection("Favorites").document(userId + "_" + plantId).get().addOnSuccessListener { doc ->
                tvFavoriteIcon.text = if (doc.exists()) "❤️" else "🤍"
            }
        }

        // 3. Logika Klik Tombol Favorit
        btnFavorite.setOnClickListener {
            if (plantId.isEmpty()) {
                Toast.makeText(context, "Gagal: ID Tanaman tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val favRef = db.collection("Favorites").document(userId + "_" + plantId)
            favRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    favRef.delete().addOnSuccessListener {
                        tvFavoriteIcon.text = "🤍"
                        Toast.makeText(context, "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val data = mapOf("userId" to userId, "plantId" to plantId, "type" to "plant")
                    favRef.set(data).addOnSuccessListener {
                        tvFavoriteIcon.text = "❤️"
                        Toast.makeText(context, "Ditambahkan ke favorit!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 4. Tombol Kembali
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // 5. Mengisi Data Teks ke UI
        tvDetailName.text = arguments?.getString("ARG_NAME") ?: "-"
        tvLatinName.text = arguments?.getString("ARG_LATIN") ?: "-"
        tvDescription.text = arguments?.getString("ARG_DESC") ?: "-"
        tvUsage.text = arguments?.getString("ARG_USAGE") ?: "-"
        tvWarning.text = arguments?.getString("ARG_WARNING") ?: "-"

        // 6. Membuat List Dinamis untuk Manfaat & Pengolahan
        setupDynamicList(llBenefitContainer, arguments?.getString("ARG_BENEFIT") ?: "", false)
        setupDynamicList(llProcessingContainer, arguments?.getString("ARG_PROCESSING") ?: "", true)

        // 7. Memuat Gambar Menggunakan Glide
        val imageUrl = arguments?.getString("ARG_IMAGE_URL") ?: ""
        if (imageUrl.isNotEmpty()) {
            Glide.with(requireContext()).load(imageUrl).into(ivDetailImage)
        }
    }

    private fun setupDynamicList(container: LinearLayout, data: String, isNumbered: Boolean) {
        container.removeAllViews()
        val items = data.split("\n")
        var stepCount = 1
        for (item in items) {
            if (item.trim().isNotEmpty()) {
                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, 24) }
                }
                val icon = TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams((24 * resources.displayMetrics.density).toInt(), (24 * resources.displayMetrics.density).toInt()).apply { setMargins(0, 0, 32, 0) }
                    gravity = Gravity.CENTER
                    textSize = 12f
                    setTypeface(null, Typeface.BOLD)
                }
                val shape = GradientDrawable().apply { shape = GradientDrawable.OVAL }
                if (isNumbered) {
                    icon.text = stepCount++.toString()
                    icon.setTextColor(Color.WHITE)
                    shape.setColor(Color.parseColor("#2E6A3F"))
                } else {
                    icon.text = "✓"
                    icon.setTextColor(Color.parseColor("#2E6A3F"))
                    shape.setColor(Color.parseColor("#E8F5E9"))
                }
                icon.background = shape
                val text = TextView(requireContext()).apply {
                    text = item.trim()
                    setTextColor(Color.parseColor("#4A5550"))
                    textSize = 14f
                }
                row.addView(icon)
                row.addView(text)
                container.addView(row)
            }
        }
    }
}