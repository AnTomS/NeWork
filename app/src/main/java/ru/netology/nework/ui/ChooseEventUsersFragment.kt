package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.databinding.FragmentChoosePostUsersBinding
import ru.netology.nework.adapter.ChooseUsersAdapter
import ru.netology.nework.adapter.ChooseUsersInteractionListener

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChooseEventUsersFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentChoosePostUsersBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.choose_event_users)

        val eventViewModel: EventViewModel by activityViewModels()

        eventViewModel.getUsers()

        val adapter = ChooseUsersAdapter(object : ChooseUsersInteractionListener {
            override fun check(id: Int) {
                eventViewModel.check(id)
            }

            override fun unCheck(id: Int) {
                eventViewModel.unCheck(id)
            }
        })
        binding.list.adapter = adapter

        eventViewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.loading) {
                Snackbar.make(binding.root, R.string.server_error_message, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        eventViewModel.usersList.observe(viewLifecycleOwner) {
            val newUser = adapter.itemCount < it.size
            adapter.submitList(it) {
                if (newUser) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

        binding.add.setOnClickListener {
            eventViewModel.addSpeakerIds()
            findNavController().navigateUp()
        }
        return binding.root
    }
}