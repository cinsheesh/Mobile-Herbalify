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
import retrofit2.Call //cek ntar
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBackGantiPassword)
        val etOldPassword = view.findViewById<EditText>(R.id.etOldPassword)
        val etNewPassword = view.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpanPassword)

        val sharedPref = requireActivity().getSharedPreferences("HerbalifyPrefs", Context.MODE_PRIVATE)

        // Ambil data user dari memori HP
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        val userName = sharedPref.getString("USER_NAME", "") ?: ""
        val userEmail = sharedPref.getString("USER_EMAIL", "") ?: ""
        val currentSavedPassword = sharedPref.getString("USER_PASSWORD", "") ?: ""

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSimpan.setOnClickListener {
            val oldPass = etOldPassword.text.toString().trim()
            val newPass = etNewPassword.text.toString().trim()
            val confPass = etConfirmPassword.text.toString().trim()

            // 1. Validasi Input Kosong
            if (oldPass.isEmpty() || newPass.isEmpty() || confPass.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Validasi Password Lama Cocok/Tidak
            if (oldPass != currentSavedPassword) {
                Toast.makeText(requireContext(), "Password lama salah!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Validasi Password Baru & Konfirmasi Cocok/Tidak
            if (newPass != confPass) {
                Toast.makeText(requireContext(), "Konfirmasi password baru tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. Validasi Panjang Password (Min. 8 karakter)
            if (newPass.length < 8) {
                Toast.makeText(requireContext(), "Password baru minimal 8 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kalau semua lolos, siapkan data User dengan password BARU
            val updatedUser = User(
                id = userId,
                nama = userName,     // Nama tetap yang lama
                email = userEmail,   // Email tetap yang lama
                password = newPass   // 🚀 Ini yang diganti!
            )

            // Tembak data ke MockAPI
            RetrofitClient.instance.updateUserProfile(userId, updatedUser).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        // Jika sukses di server, update juga password di memori HP!
                        sharedPref.edit().putString("USER_PASSWORD", newPass).apply()

                        Toast.makeText(requireContext(), "Password sukses diperbarui!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack() // Kembali ke Profil
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