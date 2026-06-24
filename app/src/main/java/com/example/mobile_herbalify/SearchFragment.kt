package com.example.mobile_herbalify

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import java.util.Locale

class SearchFragment : Fragment() {

    private lateinit var rvSearch: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var tvResultCount: TextView
    private val db = FirebaseFirestore.getInstance()

    private var allPlantsList = mutableListOf<Plant>()
    private var allArticlesList = mutableListOf<Article>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        rvSearch = view.findViewById(R.id.rvSearchPlants)
        etSearch = view.findViewById(R.id.etSearch)
        tvResultCount = view.findViewById(R.id.tvResultCount)

        rvSearch.layoutManager = LinearLayoutManager(requireContext())

        loadAllData()
        setupSearchLogic()

        return view
    }

    private fun loadAllData() {
        db.collection("Plant").get().addOnSuccessListener { result ->
            allPlantsList.clear()
            for (doc in result) allPlantsList.add(doc.toObject(Plant::class.java).copy(id = doc.id))
        }
        db.collection("Articles").get().addOnSuccessListener { result ->
            allArticlesList.clear()
            for (doc in result) allArticlesList.add(doc.toObject(Article::class.java).copy(id = doc.id))
        }
    }

    private fun setupSearchLogic() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase(Locale.getDefault())

                if (query.isEmpty()) {
                    rvSearch.adapter = null
                    tvResultCount.text = ""
                    return
                }

                val filteredPlants = allPlantsList.filter {
                    it.name.lowercase().contains(query) || it.latinName.lowercase().contains(query)
                }
                val filteredArticles = allArticlesList.filter {
                    it.title.lowercase().contains(query)
                }

                val total = filteredPlants.size + filteredArticles.size
                tvResultCount.text = "Ditemukan $total hasil untuk \"$query\""

                // Aksi Klik Tanaman (Sama dengan HomeFragment)
                val plantAdapter = PopularPlantAdapter(filteredPlants) { clickedPlant ->
                    val detailFragment = DetailFragment()
                    val bundle = Bundle()
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

                // Aksi Klik Artikel (Sama dengan HomeFragment)
                val articleAdapter = ArticleAdapter(filteredArticles) { clickedArticle ->
                    val detailArticleFragment = DetailArticleFragment()
                    val bundle = Bundle()
                    bundle.putString("ARG_TITLE", clickedArticle.title)
                    bundle.putString("ARG_CONTENT", clickedArticle.content)
                    bundle.putString("ARG_IMAGE_URL", clickedArticle.imageUrl)

                    detailArticleFragment.arguments = bundle
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, detailArticleFragment)
                        .addToBackStack(null)
                        .commit()
                }

                rvSearch.adapter = ConcatAdapter(plantAdapter, articleAdapter)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}