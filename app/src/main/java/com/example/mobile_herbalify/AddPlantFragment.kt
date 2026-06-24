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
import com.example.mobile_herbalify.model.Plant
import com.google.firebase.firestore.FirebaseFirestore
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AddPlantFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private val IMGBB_API_KEY = "7677933a7e74e7b228473587fb01ee87"

    private lateinit var ivAddPlantImage: ImageView
    private var imageUri: Uri? = null

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            ivAddPlantImage.setImageURI(uri) // Tampilkan di preview
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_plant, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBackFromAdd)
        val btnPickImage = view.findViewById<Button>(R.id.btnPickImage)
        val btnSave = view.findViewById<Button>(R.id.btnSavePlant)
        ivAddPlantImage = view.findViewById(R.id.ivAddPlantImage)

        val etName = view.findViewById<EditText>(R.id.etAddName)
        val etLatin = view.findViewById<EditText>(R.id.etAddLatin)
        val etCategory = view.findViewById<EditText>(R.id.etAddCategory)
        val etDesc = view.findViewById<EditText>(R.id.etAddDesc)
        val etBenefit = view.findViewById<EditText>(R.id.etAddBenefit)
        val etUsage = view.findViewById<EditText>(R.id.etAddUsage)
        val etProcessing = view.findViewById<EditText>(R.id.etAddProcessing)
        val etWarning = view.findViewById<EditText>(R.id.etAddWarning)

        btnPickImage.setOnClickListener {
            getImage.launch("image/*")
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val latin = etLatin.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val benefit = etBenefit.text.toString().trim()
            val usage = etUsage.text.toString().trim()
            val processing = etProcessing.text.toString().trim()
            val warning = etWarning.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Nama tanaman tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri != null) {
                Toast.makeText(requireContext(), "Menambahkan tanaman & mengunggah gambar...", Toast.LENGTH_SHORT).show()
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(imageUri!!)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
                        uploadToImgBBAndSave(base64Image, name, latin, category, desc, benefit, usage, processing, warning)
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error membaca gambar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                // Jika Admin tidak memilih gambar, simpan dengan imageUrl kosong
                saveNewPlantToFirestore(name, latin, category, desc, benefit, usage, processing, warning, "")
            }
        }
    }

    private fun uploadToImgBBAndSave(
        base64Image: String, name: String, latin: String, category: String,
        desc: String, benefit: String, usage: String, processing: String, warning: String
    ) {
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
                    // Potong JSON untuk dapat link
                    val rawUrl = response.substringAfter("\"display_url\":\"").substringBefore("\"").replace("\\/", "/")
                    val imageUrl = rawUrl

                    activity?.runOnUiThread {
                        saveNewPlantToFirestore(name, latin, category, desc, benefit, usage, processing, warning, imageUrl)
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

    private fun saveNewPlantToFirestore(
        name: String, latin: String, category: String, desc: String,
        benefit: String, usage: String, processing: String, warning: String, imageUrl: String
    ) {
        // BIKIN ID BARU SECARA OTOMATIS DARI FIRESTORE
        val newDocRef = db.collection("Plant").document()

        val newPlant = Plant(
            id = newDocRef.id, // Masukkan ID baru ke dalam model Plant
            name = name,
            latinName = latin,
            category = category,
            imageUrl = imageUrl,
            description = desc,
            benefits = benefit,
            usage = usage,
            processing = processing,
            warning = warning
        )

        newDocRef.set(newPlant)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Tanaman baru berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal simpan data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}