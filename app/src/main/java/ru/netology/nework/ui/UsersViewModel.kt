package ru.netology.nework.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsersViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is  List of Users Fragment"
    }
    val text: LiveData<String> = _text
}
