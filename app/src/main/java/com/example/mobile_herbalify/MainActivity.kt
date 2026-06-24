package com.example.mobile_herbalify

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Buka HomeFragment saat aplikasi pertama kali dijalankan
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Atur perpindahan layar saat menu bawah diklik
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_search -> {
                    replaceFragment(SearchFragment())
                    true
                }
                R.id.nav_favorite -> {
                    replaceFragment(FavoriteFragment())
                    true
                }
                R.id.nav_article -> {
                    replaceFragment(ArticleFragment())
                    true
                }
                R.id.nav_profile -> {
                    val sharedPref = getSharedPreferences("HerbalifyPrefs", Context.MODE_PRIVATE)
                    val roleUser = sharedPref.getString("USER_ROLE", "user")

                    // Saklar otomatis berdasarkan role
                    if (roleUser == "admin") {
                        replaceFragment(AdminFragment())
                    } else {
                        replaceFragment(ProfileFragment())
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}