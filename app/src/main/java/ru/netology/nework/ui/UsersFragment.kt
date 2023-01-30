package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.adapter.ContactAdapter
import ru.netology.nework.adapter.ContactInteractionListener
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.ui.UserProfileFragment.Companion.textArg
import ru.netology.nework.viewmodel.UserProfileViewModel

@AndroidEntryPoint
class UsersFragment : Fragment() {
    private val userViewModel: UserProfileViewModel by activityViewModels()
    lateinit var adapter: ContactAdapter

    @ExperimentalCoroutinesApi
    @ExperimentalPagingApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentUsersBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.contacts)

        adapter = ContactAdapter(object : ContactInteractionListener {
            override fun openUserProfile(id: Int) {
                val idAuthor = id.toString()
                findNavController().navigate(
                    R.id.profile,
                    Bundle().apply { textArg = idAuthor })
            }
        })
        binding.list.adapter = adapter

        userViewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.loading) {
                Snackbar.make(binding.root, R.string.server_error_message, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        userViewModel.getAllUsers()

        userViewModel.data.observe(viewLifecycleOwner) {
            println(it.toString())
            adapter.submitList(it)
        }





        return binding.root
    }

}


