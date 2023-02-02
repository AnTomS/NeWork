package ru.netology.nework.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.adapter.*
import ru.netology.nework.auth.AppAuth.Companion.avatar
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.ui.ProfileFragment.Companion.textArg
import ru.netology.nework.utils.StringArg
import ru.netology.nework.view.loadCircleCrop
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.UserProfileViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentUserProfileBinding.inflate(inflater, container, false)


        userProfileViewModel.getUserById(id)


        userProfileViewModel.userData.observe(viewLifecycleOwner) { it ->
            (activity as AppCompatActivity?)?.supportActionBar?.title = it.name
            arguments?.textArg?.let {
                val userId = it.toInt()
                userProfileViewModel.getUserById(userId)
                userProfileViewModel.getUserJobs(userId)


            }
            binding.name.text = it.name
            binding.avatar.loadCircleCrop(it.avatar)

        }

        val jobAdapter = JobAdapter(object : JobInteractionListener {
            override fun onLinkClick(url: String) {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), Uri.parse(url))
            }
        })

        binding.jobList.adapter = jobAdapter

        userProfileViewModel.jobData.observe(viewLifecycleOwner) {
            jobAdapter.submitList(it)
            binding.jobList.visibility = View.VISIBLE
        }

        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}