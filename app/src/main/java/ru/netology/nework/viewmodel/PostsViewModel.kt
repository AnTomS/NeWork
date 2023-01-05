package ru.netology.nework.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.*

import ru.netology.nework.enumiration.AttachmentType
import ru.netology.nework.model.FeedModel
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.repository.PostRepositoryImpl
import ru.netology.nework.utils.SingleLiveEvent

private val empty = PostCreateRequest(
    id = 0,
    content = "",
    coords = null,
    link = null,
    attachment = null,
    mentionIds = listOf(),
    viewed=  false

    )

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    val data: LiveData<FeedModel> =
            repository.data.map { FeedModel(it.filter(PostResponse::viewed), it.isEmpty()) }
            .asLiveData(Dispatchers.Default)

    private val edited = MutableLiveData(empty)

    private val _dataState = MutableLiveData(FeedModelState())

    val dataState: LiveData<FeedModelState>
        get() = _dataState

    @Suppress("UNREACHABLE_CODE")
    val newerCount: LiveData<Int> = repository.data.flowOn(Dispatchers.Default)
        .flatMapLatest {
            val firstId = it.firstOrNull()?.id ?: 0
            // При начальной загрузке покажем прогрессбар
            if (firstId == 0) _dataState.value = _dataState.value?.copy(loading = true)

            repository.getNeverCount(firstId)
                .onEach {
                    // Скроем прогрессбар и ошибку
                    _dataState.value = _dataState.value?.copy(loading = false, error = false)
                }.catch {
                    // При начальной загрузке покажем ошибку, если не получилось
                    if (firstId == 0) _dataState.value = _dataState.value?.copy(error = true)
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
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }

    }

    fun edit(post: PostResponse) {
        TODO()
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Int) {
//        if (data.value?.posts.orEmpty().filter { id == id }.none { it.likedByMe }) {
//            viewModelScope.launch {
//                try {
//                    TODO()
//                    //repository.likeByIdAsync(id)
//                } catch (e: Exception) {
//                    _dataState.value = FeedModelState(error = true)
//                }
//            }
//        } else {
//            viewModelScope.launch {
//                try {
//                    TODO()
//                    //repository.dislikeByIdAsync(id)
//                } catch (e: Exception) {
//                    _dataState.value = FeedModelState(error = true)
//                }
//            }
//        }
    }


    @Suppress("UNREACHABLE_CODE")
    fun removeById(id: Int) {
//        val posts = data.value?.posts.orEmpty()
//        //.filter { it.id != id }
//        TODO()
//        data.value?.copy(posts = posts, empty = posts.isEmpty())
//
//        viewModelScope.launch {
//            try {
//                TODO()
//                //repository.removeByIdAsync(id)
//            } catch (e: Exception) {
//                _dataState.value = FeedModelState(error = true)
//            }
//        }
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



