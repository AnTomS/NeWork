package ru.netology.nework.repository.auth


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