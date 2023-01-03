package ru.netology.nework.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Post
import ru.netology.nework.model.FeedModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.repository.PostRepositoryImpl
import ru.netology.nework.utils.SingleLiveEvent

private val empty = Post(
    id = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = "",
    likedByMe = false,
    likes = 0,
    viewed = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    val data: LiveData<FeedModel> =
        repository.data.map { FeedModel(it.filter(Post::viewed), it.isEmpty()) }
            .asLiveData(Dispatchers.Default)

    private val edited = MutableLiveData(empty)

    private val _dataState = MutableLiveData(FeedModelState())

    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val newerCount: LiveData<Int> = repository.data.flowOn(Dispatchers.Default)
        .flatMapLatest {
            val firstId = it.firstOrNull()?.id ?: 0L
            // При начальной загрузке покажем прогрессбар
            if (firstId == 0L) _dataState.value = _dataState.value?.copy(loading = true)
            repository.getNeverCount(firstId)
                .onEach {
                    // Скроем прогрессбар и ошибку
                    _dataState.value = _dataState.value?.copy(loading = false, error = false)
                }.catch {
                    // При начальной загрузке покажем ошибку, если не получилось
                    if (firstId == 0L) _dataState.value = _dataState.value?.copy(error = true)
                }
        }
        .asLiveData()


    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val scope = MainScope()

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {

        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAllAsync()
            //repository.readNewPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }

    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAllAsync()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let {

            viewModelScope.launch {
                try {
                    _postCreated.value = Unit
                    repository.saveAsync(it)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        if (data.value?.posts.orEmpty().filter { it.id == id }.none { it.likedByMe }) {
            viewModelScope.launch {
                try {
                    repository.likeByIdAsync(id)
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        } else {
            viewModelScope.launch {
                try {
                    repository.dislikeByIdAsync(id)
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
    }


    fun removeById(id: Long) {
        val posts = data.value?.posts.orEmpty()
            .filter { it.id != id }
        data.value?.copy(posts = posts, empty = posts.isEmpty())

        viewModelScope.launch {
            try {
                repository.removeByIdAsync(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun readNewPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.readNewPosts()

            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}



