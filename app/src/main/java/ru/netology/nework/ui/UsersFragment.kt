package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.adapter.ContactAdapter
import ru.netology.nework.adapter.UsersListAdapter
import ru.netology.nework.adapter.UsersListInteractionListener
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.ui.UserProfileFragment.Companion.textArg
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.UserProfileViewModel
import ru.netology.nework.viewmodel.UsersViewModel
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class UsersFragment :  Fragment() {
    val userViewModel: UserProfileViewModel by activityViewModels()
    lateinit var adapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel.data.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

}