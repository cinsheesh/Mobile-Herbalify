package com.example.mobile_herbalify

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_herbalify.adapter.ArticleAdapter
import com.example.mobile_herbalify.model.Article
import com.google.firebase.firestore.FirebaseFirestore

class ArticleFragment : Fragment() {

    private lateinit var rvAllArticles: RecyclerView

    // Inisialisasi Firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article, container, false)

        rvAllArticles = view.findViewById(R.id.rvAllArticles)
        rvAllArticles.layoutManager = LinearLayoutManager(requireContext())

        // Ambil data artikel langsung dari Firebase
        loadFirebaseArticlesData()

        return view
    }

    private fun loadFirebaseArticlesData() {
        db.collection("Articles")
            .get()
            .addOnSuccessListener { result ->
                val listArticleFromFirebase = mutableListOf<Article>()

                for (document in result) {
                    val article = document.toObject(Article::class.java).copy(id = document.id)
                    listArticleFromFirebase.add(article)
                }

                rvAllArticles.adapter = ArticleAdapter(listArticleFromFirebase) { clickedArticle ->
                    val detailArticleFragment = DetailArticleFragment()
                    val bundle = Bundle()

                    bundle.putString("ARG_ID", clickedArticle.id)

                    bundle.putString("ARG_TITLE", clickedArticle.title)
                    bundle.putString("ARG_CONTENT", clickedArticle.content)
                    bundle.putString("ARG_IMAGE_URL", clickedArticle.imageUrl)

                    detailArticleFragment.arguments = bundle

                    // Transisi pindah ke halaman DetailArticleFragment
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, detailArticleFragment)
                        .addToBackStack(null)
                        .commit()
                }

                Log.d("ArticleFragment", "Berhasil memuat ${listArticleFromFirebase.size} artikel ke list!")
            }
            .addOnFailureListener { exception ->
                Log.w("ArticleFragment", "Gagal mengambil data artikel.", exception)
                Toast.makeText(requireContext(), "Gagal memuat artikel", Toast.LENGTH_SHORT).show()
            }
    }
}