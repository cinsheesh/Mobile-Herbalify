package com.example.mobile_herbalify.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT      // <-- IMPORT INI YANG KURANG
import retrofit2.http.Path     // <-- IMPORT INI YANG KURANG
import com.example.mobile_herbalify.model.User

interface ApiService {

    @GET("users")
    fun getUsers(): Call<List<User>>

    @POST("users")
    fun registerUser(
        @Body user: User
    ): Call<User>

    @PUT("users/{id}")
    fun updateUserProfile(
        @Path("id") userId: String,
        @Body user: User
    ): Call<User>
}