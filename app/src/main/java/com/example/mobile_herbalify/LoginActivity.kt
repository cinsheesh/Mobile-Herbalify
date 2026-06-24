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

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnDaftar = findViewById<TextView>(R.id.btnDaftar)

        btnDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            RetrofitClient.instance.getUsers()
                .enqueue(object : Callback<List<User>> {

                    override fun onResponse(
                        call: Call<List<User>>,
                        response: Response<List<User>>
                    ) {

                        val users = response.body()

                        val user = users?.find {
                            it.email == email &&
                                    it.password == password
                        }

                        if (user != null) {
                            val sharedPref = getSharedPreferences("HerbalifyPrefs", MODE_PRIVATE)
                            val editor = sharedPref.edit()

                            // 🚀 INI DIA TAMBAHANNYA: Simpan ID dan Password!
                            editor.putString("USER_ID", user.id)
                            editor.putString("USER_PASSWORD", user.password)

                            // Yang lama tetap ada
                            editor.putString("USER_NAME", user.nama)
                            editor.putString("USER_EMAIL", user.email)

                            if (user.email == "admin@gmail.com") {
                                // Jika emailnya admin@gmail.com, paksa jadikan dia Admin
                                editor.putString("USER_ROLE", "admin")
                            } else {
                                // Jika email lain, jadikan User biasa (atau ambil dari data API temanmu)
                                editor.putString("USER_ROLE", "user")
                            }

                            editor.apply()

                            Toast.makeText(
                                this@LoginActivity,
                                "Login berhasil",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    MainActivity::class.java
                                )
                            )

                            finish()

                        } else {

                            Toast.makeText(
                                this@LoginActivity,
                                "Email atau password salah",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<List<User>>,
                        t: Throwable
                    ) {
                        Toast.makeText(
                            this@LoginActivity,
                            t.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }
}