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
import com.example.mobile_herbalify.model.Article
import com.google.firebase.firestore.FirebaseFirestore
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AddArticleFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val IMGBB_API_KEY = "7677933a7e74e7b228473587fb01ee87"

    private lateinit var ivAddArticleImage: ImageView
    private var imageUri: Uri? = null

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            ivAddArticleImage.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_article, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBackFromAddArticle)
        val btnPickImage = view.findViewById<Button>(R.id.btnPickArticleImage)
        val btnSave = view.findViewById<Button>(R.id.btnSaveArticle)
        ivAddArticleImage = view.findViewById(R.id.ivAddArticleImage)

        val etTitle = view.findViewById<EditText>(R.id.etAddArticleTitle)
        val etContent = view.findViewById<EditText>(R.id.etAddArticleContent)

        btnPickImage.setOnClickListener {
            getImage.launch("image/*")
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "Judul dan isi artikel tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri != null) {
                Toast.makeText(requireContext(), "Mengunggah gambar artikel...", Toast.LENGTH_SHORT).show()
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(imageUri!!)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
                        uploadToImgBBAndSaveArticle(base64Image, title, content)
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error membaca gambar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                saveArticleToFirestore(title, content, "")
            }
        }
    }

    private fun uploadToImgBBAndSaveArticle(base64Image: String, title: String, content: String) {
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
                    // Trik aman HTTP
                    val rawUrl = response.substringAfter("\"display_url\":\"").substringBefore("\"").replace("\\/", "/")
                    val imageUrl = rawUrl.replace("https://", "http://")

                    activity?.runOnUiThread {
                        saveArticleToFirestore(title, content, imageUrl)
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Gagal upload ke ImgBB", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error jaringan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun saveArticleToFirestore(title: String, content: String, imageUrl: String) {
        val newDocRef = db.collection("Articles").document()

        val newArticle = Article(
            id = newDocRef.id,
            title = title,
            content = content,
            imageUrl = imageUrl
        )

        newDocRef.set(newArticle)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Artikel berhasil dipublikasikan!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal menyimpan artikel: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}