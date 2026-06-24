package com.example.mobile_herbalify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_herbalify.adapter.AdminPlantAdapter
import com.example.mobile_herbalify.adapter.AdminArticleAdapter
import com.example.mobile_herbalify.model.Article
import com.example.mobile_herbalify.model.Plant
import com.google.firebase.firestore.FirebaseFirestore

class AdminFragment : Fragment() {

    private lateinit var rvAdminPlants: RecyclerView
    private lateinit var tvTotalPlantsAdmin: TextView
    private lateinit var rvAdminArticles: RecyclerView
    private lateinit var tvTotalArticlesAdmin: TextView

    private lateinit var tvStatTotalPlants: TextView
    private lateinit var tvStatTotalArticles: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvStatTotalPlants = view.findViewById(R.id.tvStatTotalPlants)
        tvStatTotalArticles = view.findViewById(R.id.tvStatTotalArticles)

        rvAdminPlants = view.findViewById(R.id.rvAdminPlants)
        tvTotalPlantsAdmin = view.findViewById(R.id.tvTotalPlantsAdmin)
        rvAdminPlants.layoutManager = LinearLayoutManager(requireContext())

        rvAdminArticles = view.findViewById(R.id.rvAdminArticles)
        tvTotalArticlesAdmin = view.findViewById(R.id.tvTotalArticlesAdmin)
        rvAdminArticles.layoutManager = LinearLayoutManager(requireContext())

        val btnAdminLogout = view.findViewById<Button>(R.id.btnAdminLogout)
        btnAdminLogout.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences("HerbalifyPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        val btnAddPlant = view.findViewById<Button>(R.id.btnAddPlant)
        btnAddPlant.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddPlantFragment())
                .addToBackStack(null)
                .commit()
        }

        val btnAddArticle = view.findViewById<Button>(R.id.btnAddArticle)
        btnAddArticle.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddArticleFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        loadAdminPlantsData()
        loadAdminArticlesData()
    }

    private fun loadAdminPlantsData() {
        db.collection("Plant")
            .get()
            .addOnSuccessListener { result ->
                val listPlant = mutableListOf<Plant>()
                for (document in result) {
                    val plant = document.toObject(Plant::class.java).copy(id = document.id)
                    listPlant.add(plant)
                }

                tvStatTotalPlants.text = "🌿 ${listPlant.size}"
                tvTotalPlantsAdmin.text = "${listPlant.size} tanaman terdaftar"

                rvAdminPlants.adapter = AdminPlantAdapter(
                    plantList = listPlant,
                    onEditClick = { plantToEdit ->
                        val editPlantFragment = EditPlantFragment()
                        val bundle = Bundle()
                        bundle.putString("ARG_ID", plantToEdit.id)
                        bundle.putString("ARG_NAME", plantToEdit.name)
                        bundle.putString("ARG_LATIN", plantToEdit.latinName)
                        bundle.putString("ARG_CATEGORY", plantToEdit.category)
                        bundle.putString("ARG_DESC", plantToEdit.description)
                        bundle.putString("ARG_BENEFIT", plantToEdit.benefits)
                        bundle.putString("ARG_USAGE", plantToEdit.usage)
                        bundle.putString("ARG_PROCESSING", plantToEdit.processing)
                        bundle.putString("ARG_WARNING", plantToEdit.warning)
                        bundle.putString("ARG_IMAGE_URL", plantToEdit.imageUrl)

                        editPlantFragment.arguments = bundle
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, editPlantFragment)
                            .addToBackStack(null)
                            .commit()
                    },
                    onDeleteClick = { plantToDelete ->
                        AlertDialog.Builder(requireContext())
                            .setTitle("Hapus Tanaman")
                            .setMessage("Yakin ingin menghapus ${plantToDelete.name}?")
                            .setPositiveButton("Hapus") { _, _ ->
                                db.collection("Plant").document(plantToDelete.id).delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Berhasil dihapus!", Toast.LENGTH_SHORT).show()
                                        loadAdminPlantsData()
                                    }
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                )
            }
    }

    private fun loadAdminArticlesData() {
        db.collection("Articles")
            .get()
            .addOnSuccessListener { result ->
                val listArticle = mutableListOf<Article>()
                for (document in result) {
                    val article = document.toObject(Article::class.java).copy(id = document.id)
                    listArticle.add(article)
                }

                tvStatTotalArticles.text = "📰 ${listArticle.size}"
                tvTotalArticlesAdmin.text = "${listArticle.size} artikel terdaftar"

                rvAdminArticles.adapter = AdminArticleAdapter(
                    articleList = listArticle,
                    onEditClick = { articleToEdit ->
                        val editArticleFragment = EditArticleFragment()
                        val bundle = Bundle()
                        bundle.putString("ARG_ID", articleToEdit.id)
                        bundle.putString("ARG_TITLE", articleToEdit.title)
                        bundle.putString("ARG_CONTENT", articleToEdit.content)
                        bundle.putString("ARG_IMAGE_URL", articleToEdit.imageUrl)

                        editArticleFragment.arguments = bundle
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, editArticleFragment)
                            .addToBackStack(null)
                            .commit()
                    },
                    onDeleteClick = { articleToDelete ->
                        AlertDialog.Builder(requireContext())
                            .setTitle("Hapus Artikel")
                            .setMessage("Yakin ingin menghapus artikel '${articleToDelete.title}'?")
                            .setPositiveButton("Hapus") { _, _ ->
                                db.collection("Articles").document(articleToDelete.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Artikel berhasil dihapus!", Toast.LENGTH_SHORT).show()
                                        loadAdminArticlesData()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "Gagal menghapus artikel", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                )
            }
            .addOnFailureListener { exception ->
                Log.w("AdminFragment", "Gagal memuat artikel admin.", exception)
            }
    }
}
