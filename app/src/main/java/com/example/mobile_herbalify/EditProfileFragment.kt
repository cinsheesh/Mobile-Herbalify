package com.example.mobile_herbalify

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mobile_herbalify.model.User
import com.example.mobile_herbalify.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBackEditProfile)
        val etEditName = view.findViewById<EditText>(R.id.etEditName)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpanProfil)

        val sharedPref = requireActivity().getSharedPreferences("HerbalifyPrefs", Context.MODE_PRIVATE)

        // Ambil data user dari lokal HP
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        val currentName = sharedPref.getString("USER_NAME", "")
        val userEmail = sharedPref.getString("USER_EMAIL", "") ?: ""
        val userPassword = sharedPref.getString("USER_PASSWORD", "") ?: ""
//
        // Set text nama otomatis
        etEditName.setText(currentName)

        // Aksi tombol kembali
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Aksi tombol simpan
        btnSimpan.setOnClickListener {
            val newName = etEditName.text.toString().trim()

            // 1. Validasi Nama Kosong
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Validasi ID Kosong (Mencegah error tembak API)
            if (userId.isEmpty()) {
                Toast.makeText(requireContext(), "Gagal: ID User kosong! Silakan Logout dan Login ulang.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Siapkan objek User dengan nama yang diupdate
            val updatedUser = User(
                id = userId,
                nama = newName,
                email = userEmail,
                password = userPassword
            )

            // Tembak data ke MockAPI
            RetrofitClient.instance.updateUserProfile(userId, updatedUser).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        // Simpan nama baru di lokal HP
                        sharedPref.edit().putString("USER_NAME", newName).apply()

                        Toast.makeText(requireContext(), "Profil sukses diperbarui!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Gagal memperbarui: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error Jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}