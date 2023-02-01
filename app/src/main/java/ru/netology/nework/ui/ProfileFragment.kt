package ru.netology.nework.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.adapter.JobAdapter
import ru.netology.nework.adapter.JobInteractionListener
import ru.netology.nework.databinding.FragmentProfileBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.utils.StringArg
import ru.netology.nework.view.loadCircleCrop
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.UserProfileViewModel

class ProfileFragment : Fragment() {
    val userProfileViewModel: UserProfileViewModel by activityViewModels()
    val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!authViewModel.authenticated && arguments == null)
            findNavController().navigate(R.id.list_of_users)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        authViewModel.data.observeForever {
            if (!authViewModel.authenticated || arguments != null) {
                binding.addJob.visibility = View.GONE

            } else if (authViewModel.authenticated && arguments == null) {
                binding.addJob.visibility = View.VISIBLE
//                binding.addPost.visibility = View.VISIBLE
                val myId = userProfileViewModel.myId.toInt()
                userProfileViewModel.getUserById(myId)
                userProfileViewModel.getMyJobs()

            }
        }

        val jobAdapter = JobAdapter(object : JobInteractionListener {
            override fun onLinkClick(url: String) {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), Uri.parse(url))
            }

            override fun onRemoveJob(job: Job) {
                userProfileViewModel.removeJobById(job.id)
            }
        })

        binding.jobList.adapter = jobAdapter

        userProfileViewModel.jobData.observe(viewLifecycleOwner) {
            if (authViewModel.authenticated && arguments == null) {
                it.forEach { job ->
                    job.ownedByMe = true
                }
            }
            if (it.isEmpty()) {
                binding.jobList.visibility = View.GONE
            } else {
                jobAdapter.submitList(it)
                binding.jobList.visibility = View.VISIBLE
            }
        }

        userProfileViewModel.userData.observe(viewLifecycleOwner) {
            (activity as AppActivity?)?.supportActionBar?.title = it.name
            binding.name.text = it.name
            binding.avatar.loadCircleCrop(it.avatar)
        }

        binding.addJob.setOnClickListener {
            findNavController().navigate(R.id.newJobFragment)
        }


        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}