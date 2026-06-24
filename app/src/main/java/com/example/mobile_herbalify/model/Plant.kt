package com.example.mobile_herbalify.model

data class Plant(
    val id: String = "",
    val name: String = "",
    val latinName: String = "",
    val category: String = "",
    val imageResId: Int = 0,
    val imageUrl: String = "",

    // Tambahan wadah untuk data DetailActivity (Wajib ada default string "")
    val description: String = "",
    val benefits: String = "",
    val usage: String = "",
    val processing: String = "",
    val warning: String = ""
)