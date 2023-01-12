package ru.netology.nework.repository

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.Part
import ru.netology.nework.auth.AuthState
import ru.netology.nework.dto.MediaUpload

interface AuthRepository {

    suspend fun signIn(
        login: String,
        pass: String,
    ): AuthState

    suspend fun register(
        login: String,
        pass: String,
        name: String,
    ): AuthState

    suspend fun registerWithPhoto(
        login: String,
        pass: String,
        name: String,
        media: MediaUpload,
    ): AuthState
}