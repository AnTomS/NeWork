package ru.netology.nework.repository


import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.PostsApi
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnkError
import java.io.IOException


class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {
    override val data: Flow<List<Post>> = postDao.getAll().map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override fun getNeverCount(firstId: Long): Flow<Int> = flow {
        try {
            while (true) {
                val response = PostsApi.retrofitService.getNewer(firstId)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }

                val body =
                    response.body() ?: throw ApiError(response.code(), response.message())
                postDao.insert(body.toEntity()
                    .let { posts ->
                        val empty = postDao.isEmpty()
                        // Если это начальная загрузка, покажем все посты.
                        // Иначе скроем до нажатия на кнопку
                        posts.map { it.copy(viewed = empty) }
                    }
                )
                // Прочитаем из БД сколько действительно постов непрочитано.
                // То, что вернул сервер может быть меньше
                emit(postDao.getUnreadCount())
                emit(body.size)
                delay(10_000L)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }
        .flowOn(Dispatchers.Default)


    override suspend fun getAllAsync() {

        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity()
                .map {
                    it.copy(viewed = true)
                }
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun readNewPosts() {
        try {
            postDao.viewedPosts()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }


    override suspend fun saveAsync(post: Post) {
        try {
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(
                PostEntity.fromDto(body)
                //      .copy(viewed = true)
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun removeByIdAsync(id: Long) {
        postDao.removeById(id)
        try {

            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun likeByIdAsync(id: Long) {
        postDao.likeById(id)
        try {
            val response = PostsApi.retrofitService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(
                PostEntity.fromDto(body)
                    .copy(viewed = true)
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun dislikeByIdAsync(id: Long) {
        postDao.likeById(id)
        try {
            val response = PostsApi.retrofitService.dislikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(
                PostEntity.fromDto(body)
                    .copy(viewed = true)
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun shareById(id: Long) {

        try {
            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(
                    response.code(), response.message()
                )
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }
}
