package com.example.mobile_herbalify


import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.mobile_herbalify.R
import com.example.mobile_herbalify.model.User
import com.example.mobile_herbalify.network.ApiService
import com.example.mobile_herbalify.network.RetrofitClient

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etNama = findViewById<EditText>(R.id.etNama)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etKonfirmasi = findViewById<EditText>(R.id.etKonfirmasi)

        val btnDaftar = findViewById<Button>(R.id.btnDaftarSekarang)
        val btnMasuk = findViewById<TextView>(R.id.btnMasuk)

        btnMasuk.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnDaftar.setOnClickListener {

            val nama = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val konfirmasi = etKonfirmasi.text.toString().trim()

            if (nama.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                konfirmasi.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Semua field wajib diisi",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != konfirmasi) {
                Toast.makeText(
                    this,
                    "Password tidak sama",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val user = User(
                nama = nama,
                email = email,
                password = password
            )

            RetrofitClient.instance.registerUser(user)
                .enqueue(object : Callback<User> {

                    override fun onResponse(
                        call: Call<User>,
                        response: Response<User>
                    ) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registrasi berhasil",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                this@RegisterActivity,
                                LoginActivity::class.java
                            )
                        )

                        finish()
                    }

                    override fun onFailure(
                        call: Call<User>,
                        t: Throwable
                    ) {
                        Toast.makeText(
                            this@RegisterActivity,
                            t.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }
}