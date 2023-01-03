package ru.netology.nework.repository


import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Post


interface PostRepository {

    val data: Flow<List<Post>>
    fun getNeverCount(firstId: Long): Flow<Int>
    suspend fun getAllAsync()
    suspend fun readNewPosts()
    suspend fun likeByIdAsync(id: Long)
    suspend fun dislikeByIdAsync(id: Long)
    suspend fun shareById(id: Long)
    suspend fun saveAsync(post: Post)
    suspend fun removeByIdAsync(id: Long)
}
