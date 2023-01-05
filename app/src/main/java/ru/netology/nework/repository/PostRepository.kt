package ru.netology.nework.repository


import kotlinx.coroutines.flow.Flow

import ru.netology.nework.dto.PostResponse


interface PostRepository {

    val data: Flow<List<PostResponse>>
    fun getNeverCount(firstId: Int): Flow<Int>
    suspend fun getAllAsync()
    suspend fun readNewPosts()
    suspend fun likeByIdAsync(id: Int)
    suspend fun dislikeByIdAsync(id: Int)
    suspend fun shareById(id: Int)
    suspend fun saveAsync(id: Int)
    suspend fun removeByIdAsync(id: Int)
}
