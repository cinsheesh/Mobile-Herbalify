package com.example.mobile_herbalify

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mobile_herbalify.model.Article
import com.google.firebase.firestore.FirebaseFirestore
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class EditArticleFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val IMGBB_API_KEY = "7677933a7e74e7b228473587fb01ee87"

    private lateinit var ivEditArticleImage: ImageView
    private var imageUri: Uri? = null
    private var oldImageUrl: String = ""
    private var articleId: String = ""

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            ivEditArticleImage.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_article, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBackFromEditArticle)
        val btnPickImage = view.findViewById<Button>(R.id.btnPickEditArticleImage)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdateArticle)
        ivEditArticleImage = view.findViewById(R.id.ivEditArticleImage)

        val etTitle = view.findViewById<EditText>(R.id.etEditArticleTitle)
        val etContent = view.findViewById<EditText>(R.id.etEditArticleContent)

        // 1. Tangkap data lama yang dikirim dari AdminFragment
        articleId = arguments?.getString("ARG_ID") ?: ""
        etTitle.setText(arguments?.getString("ARG_TITLE"))
        etContent.setText(arguments?.getString("ARG_CONTENT"))

        // 2. Tampilkan gambar lama menggunakan Glide
        oldImageUrl = arguments?.getString("ARG_IMAGE_URL") ?: ""
        if (oldImageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(oldImageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivEditArticleImage)
        }

        // Aksi Tombol
        btnPickImage.setOnClickListener { getImage.launch("image/*") }
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        btnUpdate.setOnClickListener {
            checkAndUploadArticleUpdate(etTitle, etContent)
        }
    }

    private fun checkAndUploadArticleUpdate(etTitle: EditText, etContent: EditText) {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (title.isEmpty() || articleId.isEmpty()) {
            Toast.makeText(requireContext(), "Judul tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        // Jika admin memilih gambar baru
        if (imageUri != null) {
            Toast.makeText(requireContext(), "Mengunggah gambar baru...", Toast.LENGTH_SHORT).show()
            try {
                val inputStream = requireContext().contentResolver.openInputStream(imageUri!!)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
                    uploadToImgBBAndUpdate(base64Image, title, content)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error membaca gambar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            // Jika tidak ada gambar baru, gunakan URL gambar yang lama
            saveUpdateToFirestore(title, content, oldImageUrl)
        }
    }

    private fun uploadToImgBBAndUpdate(base64Image: String, title: String, content: String) {
        Thread {
            try {
                val url = URL("https://api.imgbb.com/1/upload")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData = "key=$IMGBB_API_KEY&image=" + URLEncoder.encode(base64Image, "UTF-8")

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(postData)
                writer.flush()
                writer.close()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()

                    // Jurus HTTP untuk bypass blokir
                    val rawUrl = response.substringAfter("\"display_url\":\"").substringBefore("\"").replace("\\/", "/")
                    val imageUrl = rawUrl.replace("https://", "http://")

                    activity?.runOnUiThread {
                        saveUpdateToFirestore(title, content, imageUrl)
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Gagal upload ke ImgBB", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error ImgBB: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun saveUpdateToFirestore(title: String, content: String, imageUrl: String) {
        val updatedArticle = Article(
            id = articleId,
            title = title,
            content = content,
            imageUrl = imageUrl
        )

        db.collection("Articles").document(articleId)
            .set(updatedArticle)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Artikel berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal update artikel: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}