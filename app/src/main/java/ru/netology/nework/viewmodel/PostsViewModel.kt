package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.*
import ru.netology.nework.enumiration.AttachmentType
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.MediaModel
import ru.netology.nework.repository.post.PostRepository
import ru.netology.nework.utils.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val editedPost = PostCreateRequest(
    id = 0,
    content = "",
    link = null,
    attachment = null,
    mentionIds = listOf()
)

private val mentions = mutableListOf<UserResponse>()

private val noMedia = MediaModel()

@ExperimentalCoroutinesApi
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _postResponse = MutableLiveData<PostResponse>()
    val postResponse: LiveData<PostResponse>
        get() = _postResponse

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    val postUsersData: LiveData<List<UserPreview>> = repository.postUsersData

    var isPostIntent = false

    val newPost: MutableLiveData<PostCreateRequest> = MutableLiveData(editedPost)
    val usersList: MutableLiveData<List<UserResponse>> = MutableLiveData()
    val mentionsData: MutableLiveData<MutableList<UserResponse>> = MutableLiveData()

    val data: Flow<PagingData<PostResponse>> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            val cached = repository.data.cachedIn(viewModelScope)
            cached.map { pagingData ->
                pagingData.map {
                    it.copy(ownedByMe = it.authorId.toLong() == myId)
                }

            }
        }

    fun getLikedAndMentionedUsersList(post: PostResponse) {
        viewModelScope.launch {
            try {
                repository.getLikedAndMentionedUsersList(post)
                _dataState.value = FeedModelState(loading = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(loading = true)
            }
        }
    }

    fun removePostById(id: Int) {
        viewModelScope.launch {
            try {
                repository.removePostById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun likePostById(id: Int) {
        viewModelScope.launch {
            try {
                _postResponse.postValue(repository.likePostById(id))
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun dislikePostById(id: Int) {
        viewModelScope.launch {
            try {
                _postResponse.postValue(repository.dislikePostById(id))
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getPostCreateRequest(id: Int) {
        mentionsData.postValue(mentions)
        viewModelScope.launch {
            try {
                newPost.value = repository.getPostCreateRequest(id)
                newPost.value?.mentionIds?.forEach {
                    mentionsData.value!!.add(repository.getUserById(it))
                }
                _dataState.value = FeedModelState(error = false)
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getUsers() {
        mentionsData.postValue(mentions)
        viewModelScope.launch {
            try {
                usersList.value = repository.getUsers()
                usersList.value?.forEach { user ->
                    newPost.value?.mentionIds?.forEach {
                        if (user.id == it) {
                            user.isChecked = true
                        }
                    }
                }
                _dataState.value = FeedModelState(error = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun addMentionIds() {
        mentionsData.postValue(mentions)
        val listChecked = mutableListOf<Int>()
        val mentionsUserList = mutableListOf<UserResponse>()
        usersList.value?.forEach { user ->
            if (user.isChecked) {
                listChecked.add(user.id)
                mentionsUserList.add(user)
            }
        }
        mentionsData.postValue(mentionsUserList)
        newPost.value = newPost.value?.copy(mentionIds = listChecked)
    }

    fun check(id: Int) {
        usersList.value?.forEach {
            if (it.id == id) {
                it.isChecked = true
            }
        }
    }

    fun unCheck(id: Int) {
        usersList.value?.forEach {
            if (it.id == id) {
                it.isChecked = false
            }
        }
    }

    fun addLink(link: String) {
        if (link != "") {
            newPost.value = newPost.value?.copy(link = link)
        } else {
            newPost.value = newPost.value?.copy(link = null)
        }
    }

    fun changeMedia(uri: Uri?, file: File?, type: AttachmentType?) {
        _media.value = MediaModel(uri, file, type)
    }

    fun savePost(content: String) {
        newPost.value = newPost.value?.copy(content = content)
        val post = newPost.value!!
        viewModelScope.launch {
            try {
                repository.savePost(post)
                _dataState.value = FeedModelState(error = false)
                deleteEditPost()
                _postCreated.value = Unit
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun addMediaToPost(
        type: AttachmentType,
        file: MultipartBody.Part,
    ) {
        viewModelScope.launch {
            try {
                val attachment = repository.addMediaToPost(type, file)
                newPost.value = newPost.value?.copy(attachment = attachment)
                _dataState.value = FeedModelState(error = false)
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun deleteEditPost() {
        newPost.value = editedPost
        mentions.clear()
        mentionsData.postValue(mentions)
    }
}