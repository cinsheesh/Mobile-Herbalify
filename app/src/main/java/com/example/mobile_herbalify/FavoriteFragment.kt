package com.example.mobile_herbalify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_herbalify.adapter.ArticleAdapter
import com.example.mobile_herbalify.adapter.PopularPlantAdapter
import com.example.mobile_herbalify.model.Article
import com.example.mobile_herbalify.model.Plant
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteFragment : Fragment() {

    private lateinit var rvFavorites: RecyclerView
    private lateinit var tvNoFavorites: TextView
    private val db = FirebaseFirestore.getInstance()
    private val userId = "user123" // Pasikan ID ini sama dengan yang di DetailFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        rvFavorites = view.findViewById(R.id.rvFavorites) // Sesuaikan dengan ID RecyclerView di XML kamu
        tvNoFavorites = view.findViewById(R.id.tvNoFavorites) // Sesuaikan dengan ID TextView kosong di XML kamu

        rvFavorites.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onResume() {
        super.onResume()
        // Load ulang data setiap kali user membuka/kembali ke fragment ini
        loadFavoriteData()
    }

    private fun loadFavoriteData() {
        // 1. Ambil semua data favorit milik user ini
        db.collection("Favorites")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { favResult ->
                if (favResult.isEmpty) {
                    showEmptyState(true)
                    return@addOnSuccessListener
                }

                val plantIds = mutableListOf<String>()
                val articleIds = mutableListOf<String>()

                // Pisahkan ID berdasarkan tipenya
                for (doc in favResult) {
                    val type = doc.getString("type")
                    val plantId = doc.getString("plantId")
                    val articleId = doc.getString("articleId")

                    if (type == "plant" && plantId != null) plantIds.add(plantId)
                    if (type == "article" && articleId != null) articleIds.add(articleId)
                }

                fetchDetails(plantIds, articleIds)
            }
            .addOnFailureListener {
                showEmptyState(true)
            }
    }

    private fun fetchDetails(plantIds: List<String>, articleIds: List<String>) {
        val filteredPlants = mutableListOf<Plant>()
        val filteredArticles = mutableListOf<Article>()

        // Menggunakan flag counter untuk memastikan kedua query Firebase selesai sebelum update UI
        var queriesToLoad = 0
        if (plantIds.isNotEmpty()) queriesToLoad++
        if (articleIds.isNotEmpty()) queriesToLoad++

        if (queriesToLoad == 0) {
            showEmptyState(true)
            return
        }

        val onQueryComplete = {
            queriesToLoad--
            if (queriesToLoad == 0) {
                if (filteredPlants.isEmpty() && filteredArticles.isEmpty()) {
                    showEmptyState(true)
                } else {
                    showEmptyState(false)
                    setupAdapter(filteredPlants, filteredArticles)
                }
            }
        }

        // Query Detail Tanaman
        if (plantIds.isNotEmpty()) {
            db.collection("Plant")
                .whereIn("id", plantIds)
                .get()
                .addOnSuccessListener { result ->
                    for (doc in result) {
                        filteredPlants.add(doc.toObject(Plant::class.java).copy(id = doc.id))
                    }
                    onQueryComplete()
                }
                .addOnFailureListener { onQueryComplete() }
        }

        // Query Detail Artikel
        if (articleIds.isNotEmpty()) {
            db.collection("Articles")
                .whereIn("id", articleIds)
                .get()
                .addOnSuccessListener { result ->
                    for (doc in result) {
                        filteredArticles.add(doc.toObject(Article::class.java).copy(id = doc.id))
                    }
                    onQueryComplete()
                }
                .addOnFailureListener { onQueryComplete() }
        }
    }

    private fun setupAdapter(plants: List<Plant>, articles: List<Article>) {
        // Adapter Tanaman Beserta Logika Klik Navigasinya
        val plantAdapter = PopularPlantAdapter(plants) { clickedPlant ->
            val detailFragment = DetailFragment()
            val bundle = Bundle().apply {
                putString("ARG_ID", clickedPlant.id)
                putString("ARG_NAME", clickedPlant.name)
                putString("ARG_LATIN", clickedPlant.latinName)
                putString("ARG_DESC", clickedPlant.description)
                putString("ARG_BENEFIT", clickedPlant.benefits)
                putString("ARG_USAGE", clickedPlant.usage)
                putString("ARG_PROCESSING", clickedPlant.processing)
                putString("ARG_WARNING", clickedPlant.warning)
                putString("ARG_IMAGE_URL", clickedPlant.imageUrl)
            }
            detailFragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        // Adapter Artikel Beserta Logika Klik Navigasinya
        val articleAdapter = ArticleAdapter(articles) { clickedArticle ->
            val detailArticleFragment = DetailArticleFragment()
            val bundle = Bundle().apply {
                putString("ARG_ID", clickedArticle.id)
                putString("ARG_TITLE", clickedArticle.title)
                putString("ARG_CONTENT", clickedArticle.content)
                putString("ARG_IMAGE_URL", clickedArticle.imageUrl)
            }
            detailArticleFragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailArticleFragment)
                .addToBackStack(null)
                .commit()
        }


        rvFavorites.adapter = ConcatAdapter(plantAdapter, articleAdapter)
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            rvFavorites.visibility = View.GONE
            tvNoFavorites.visibility = View.VISIBLE
            rvFavorites.adapter = null
        } else {
            rvFavorites.visibility = View.VISIBLE
            tvNoFavorites.visibility = View.GONE
        }
    }
}