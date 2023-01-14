package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.enumiration.AttachmentType
import ru.netology.nework.model.MediaModel
import ru.netology.nework.repository.auth.AuthRepository
import java.io.File
import javax.inject.Inject

private val noAvatar = MediaModel()

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val auth: AppAuth,
) : ViewModel() {
    private val _avatar = MutableLiveData(noAvatar)
    val avatar: LiveData<MediaModel>
        get() = _avatar

    fun register(login: String, pass: String, name: String) = viewModelScope.launch {
        val response = repository.register(login, pass, name)
        response.token?.let {
            auth.setAuth(response.id, response.token, response.avatar, response.name)
        }
    }


    fun registerWithPhoto(
        login: String,
        pass: String,
        name: String,
        media: MediaUpload,
    ) = viewModelScope.launch {
        val response = repository.registerWithPhoto(login, pass, name, media)
        response.token?.let {
            auth.setAuth(response.id, response.token, response.avatar, response.name)
        }
    }

    fun changeAvatar(uri: Uri?, file: File?) {
        _avatar.value = MediaModel(uri, file, AttachmentType.IMAGE)
    }
}