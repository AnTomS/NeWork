package ru.netology.nework.repository.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.entity.UserEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import java.io.IOException
import javax.inject.Inject


val emptyUser = UserResponse()


class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
) : UserRepository {
    override val data: MutableLiveData<List<UserResponse>> =
    userDao.getAllUsers().map(List<UserEntity>::toDto)  as MutableLiveData<List<UserResponse>>
    override val userData: MutableLiveData<UserResponse>
            = MutableLiveData(emptyUser)

    override suspend fun getAllUsers() {
        try {
            val response = apiService.getAllUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            data.postValue(body)
            userDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getUserById(id: Int) {
        try {
            val response = apiService.getUserById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            userData.postValue(body)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

}