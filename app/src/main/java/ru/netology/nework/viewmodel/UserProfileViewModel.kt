package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.PostResponse
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.post.PostRepository
import ru.netology.nework.repository.user.UserRepository
import ru.netology.nework.utils.SingleLiveEvent
import javax.inject.Inject

val editedJob = Job(
    id = 0,
    name = "",
    position = "",
    start = "",
    finish = null,
    link = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    //private val jobRepository: JobRepository,
    private val postRepository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    val myId: Long = appAuth.authStateFlow.value.id

    var userId: MutableLiveData<Int?> = MutableLiveData()

    val newJob: MutableLiveData<Job> = MutableLiveData(editedJob)

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    val data: MutableLiveData<List<UserResponse>> = userRepository.data
    val userData: MutableLiveData<UserResponse> = userRepository.userData

    //val jobData: MutableLiveData<List<Job>> = jobRepository.data
    var postData: Flow<PagingData<PostResponse>> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            val cached = postRepository.data.cachedIn(viewModelScope)
            cached.map { pagingData ->
                pagingData.map {
                    it.copy(ownedByMe = it.authorId.toLong() == myId)
                }
            }
        }

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    fun getAllUsers() {
        viewModelScope.launch {
            try {
                userRepository.getAllUsers()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getUserById(id: Int) {
        viewModelScope.launch {
            try {
                userRepository.getUserById(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

//    fun getUserJobs(id: Int) {
//        viewModelScope.launch {
//            try {
//                jobRepository.getUserJobs(id)
//            } catch (e: Exception) {
//                _dataState.value = FeedModelState(error = true)
//            }
//        }
//    }

//    fun saveJob(job: Job) {
//        viewModelScope.launch {
//            try {
//                jobRepository.saveJob(job)
//                _dataState.value = FeedModelState(error = false)
//                deleteEditJob()
//                _jobCreated.value = Unit
//            } catch (e: Exception) {
//                _dataState.value = FeedModelState(error = true)
//            }
//        }
//    }

//    fun removeJobById(id: Int) {
//        viewModelScope.launch {
//            try {
//                jobRepository.removeJobById(id)
//            } catch (e: Exception) {
//                _dataState.value = FeedModelState(error = true)
//            }
//        }
//    }

//    fun getMyJobs() {
//        viewModelScope.launch {
//            try {
//                jobRepository.getMyJobs()
//            } catch (e: Exception) {
//                _dataState.value = FeedModelState(error = true)
//            }
//        }
//    }

    fun deleteEditJob() {
        newJob.postValue(editedJob)
    }

    fun addStartDate(date: String) {
        newJob.value = newJob.value?.copy(start = date)
    }

    fun addEndDate(date: String) {
        newJob.value = newJob.value?.copy(finish = date)
    }

    fun getUserPosts(id: Int) {
        postData = postRepository.data
        postData = postData.map { it.filter { it.authorId == id } }
        viewModelScope.launch {
            try {
                postRepository.getUserPosts(postData, id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }
}