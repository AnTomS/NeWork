package ru.netology.nework.repository.user

import androidx.lifecycle.MutableLiveData
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.dto.UserResponse

interface UserRepository {
    val data: MutableLiveData<List<UserResponse>>
    val userData: MutableLiveData<UserResponse>
    suspend fun getAllUsers()
    suspend fun getUserById(id: Int)
}