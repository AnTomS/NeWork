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
import ru.netology.nework.dto.PostResponse
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnkError
import java.io.IOException


class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {
    override val data: Flow<List<PostResponse>> = postDao.getAll().map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override fun getNeverCount(firstId: Int): Flow<Int> = flow {
        try {
            while (true) {
                val response = PostsApi.retrofitService.getLatestPosts(firstId)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }

                val body =
                    response.body() ?: throw ApiError(response.code(), response.message())
                postDao.insert(
                    body.toEntity()

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
            val response = PostsApi.retrofitService.getAllPosts()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(
                body.toEntity()

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


    @Suppress("UNREACHABLE_CODE")
    override suspend fun saveAsync(id: Int) {
        try {
            TODO()
//            //val response = PostsApi.retrofitService.savePost()
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            postDao.insert(
//                PostEntity.fromDto(body)
//                //      .copy(viewed = true)
        //           )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun removeByIdAsync(id: Int) {
        postDao.removeById(id)
        try {

            val response = PostsApi.retrofitService.removePostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun likeByIdAsync(id: Int) {
        postDao.likeById(id)
        try {
            val response = PostsApi.retrofitService.likePostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(
                PostEntity.fromDto(body)

            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun dislikeByIdAsync(id: Int) {
        postDao.likeById(id)
        try {
            val response = PostsApi.retrofitService.dislikePostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(
                PostEntity.fromDto(body)
            )
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnkError
        }
    }

    override suspend fun shareById(id: Int) {

        try {
            val response = PostsApi.retrofitService.getPostById(id)
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
