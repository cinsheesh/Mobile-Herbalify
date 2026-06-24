package com.example.mobile_herbalify

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_herbalify.adapter.ArticleAdapter
import com.example.mobile_herbalify.adapter.PopularPlantAdapter
import com.example.mobile_herbalify.model.Article
import com.example.mobile_herbalify.model.Plant
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var rvPopularPlants: RecyclerView
    private lateinit var rvRecentArticles: RecyclerView
    private lateinit var tvNamaBeranda: TextView
    private lateinit var etSearchHome: EditText

    private var allPlantsList = mutableListOf<Plant>()
    private var allArticlesList = mutableListOf<Article>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvNamaBeranda = view.findViewById(R.id.tvHomeUsername)

        val sharedPref = requireActivity().getSharedPreferences("HerbalifyPrefs", Context.MODE_PRIVATE)
        val namaUser = sharedPref.getString("USER_NAME", "Pengguna")
        tvNamaBeranda.text = "Halo, $namaUser!"

        rvPopularPlants = view.findViewById(R.id.rvPopularPlants)
        rvRecentArticles = view.findViewById(R.id.rvRecentArticles)

        rvPopularPlants.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentArticles.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        etSearchHome = view.findViewById(R.id.etSearchHome)
        setupSearchInput()

        loadFirebasePlantsData()
        loadFirebaseArticlesData()

        return view
    }

    private fun setupSearchInput() {
        etSearchHome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterData(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        val filteredPlants = allPlantsList.filter {
            it.name.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    it.latinName.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
        }
        updatePlantAdapter(filteredPlants)

        val filteredArticles = allArticlesList.filter {
            it.title.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
        }
        updateArticleAdapter(filteredArticles)
    }

    private fun loadFirebasePlantsData() {
        db.collection("Plant")
            .get()
            .addOnSuccessListener { result ->
                allPlantsList.clear()
                for (document in result) {
                    val plant = document.toObject(Plant::class.java).copy(id = document.id)
                    allPlantsList.add(plant)
                }
                updatePlantAdapter(allPlantsList)
            }
    }

    private fun loadFirebaseArticlesData() {
        db.collection("Articles")
            .get()
            .addOnSuccessListener { result ->
                allArticlesList.clear()
                for (document in result) {
                    val article = document.toObject(Article::class.java).copy(id = document.id)
                    allArticlesList.add(article)
                }
                updateArticleAdapter(allArticlesList)
            }
    }

    // Fungsi pembantu untuk pasang adapter tanaman
    private fun updatePlantAdapter(plants: List<Plant>) {
        rvPopularPlants.adapter = PopularPlantAdapter(plants) { clickedPlant ->
            val detailFragment = DetailFragment()
            val bundle = Bundle()

            // 🚀 ID DIKIRIM DI SINI (Kunci: ARG_ID)
            bundle.putString("ARG_ID", clickedPlant.id)

            bundle.putString("ARG_NAME", clickedPlant.name)
            bundle.putString("ARG_LATIN", clickedPlant.latinName)
            bundle.putString("ARG_DESC", clickedPlant.description)
            bundle.putString("ARG_BENEFIT", clickedPlant.benefits)
            bundle.putString("ARG_USAGE", clickedPlant.usage)
            bundle.putString("ARG_PROCESSING", clickedPlant.processing)
            bundle.putString("ARG_WARNING", clickedPlant.warning)
            bundle.putString("ARG_IMAGE_URL", clickedPlant.imageUrl)

            detailFragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    // Fungsi pembantu untuk pasang adapter artikel
    private fun updateArticleAdapter(articles: List<Article>) {
        rvRecentArticles.adapter = ArticleAdapter(articles) { clickedArticle ->
            val detailArticleFragment = DetailArticleFragment()
            val bundle = Bundle()

            // 🚀 ID ARTIKEL DIKIRIM DI SINI (Kunci: ARG_ID)
            bundle.putString("ARG_ID", clickedArticle.id)

            bundle.putString("ARG_TITLE", clickedArticle.title)
            bundle.putString("ARG_CONTENT", clickedArticle.content)
            bundle.putString("ARG_IMAGE_URL", clickedArticle.imageUrl)

            detailArticleFragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailArticleFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}