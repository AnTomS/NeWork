package ru.netology.nework.ui

import android.content.Intent
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
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.adapter.PostInteractionListener
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.dto.PostResponse
import ru.netology.nework.enumiration.AttachmentType
import ru.netology.nework.ui.PostsFragment.Companion.intArg
import ru.netology.nework.utils.StringArg
import ru.netology.nework.view.loadCircleCrop
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.UserProfileViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    val userProfileViewModel: UserProfileViewModel by activityViewModels()
    val authViewModel: AuthViewModel by activityViewModels()
    val postViewModel: PostViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!authViewModel.authenticated && arguments == null)
            findNavController().navigate(R.id.list_post)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        authViewModel.data.observeForever {
            if (!authViewModel.authenticated || arguments != null) {
                binding.addJob.visibility = View.GONE
                binding.addPost.visibility = View.GONE
                arguments?.textArg?.let {
                    val userId = it.toInt()
//                    userProfileViewModel.getUserById(userId)
//                    userProfileViewModel.getUserJobs(userId)
//                    userProfileViewModel.getUserPosts(userId)
                }
            } else if (authViewModel.authenticated && arguments == null) {
                binding.addJob.visibility = View.VISIBLE
                binding.addPost.visibility = View.VISIBLE
//                val myId = userProfileViewModel.myId.toInt()
//                userProfileViewModel.getUserById(myId)
//                userProfileViewModel.getMyJobs()
//                userProfileViewModel.getUserPosts(myId)

            }
        }

//        val jobAdapter = JobAdapter(object : JobInteractionListener {
//            override fun onLinkClick(url: String) {
//                CustomTabsIntent.Builder()
//                    .setShowTitle(true)
//                    .build()
//                    .launchUrl(requireContext(), Uri.parse(url))
//            }
//
//            override fun onRemoveJob(job: Job) {
//                userProfileViewModel.removeJobById(job.id)
//            }
//        })

//        binding.jobList.adapter = jobAdapter
//
//        userProfileViewModel.jobData.observe(viewLifecycleOwner) {
//            if (authViewModel.authenticated && arguments == null) {
//                it.forEach { job ->
//                    job.ownedByMe = true
//                }
//            }
//            if (it.isEmpty()) {
//                binding.jobList.visibility = View.GONE
//            } else {
//                jobAdapter.submitList(it)
//                binding.jobList.visibility = View.VISIBLE
//            }
//        }
//
//        userProfileViewModel.userData.observe(viewLifecycleOwner) {
//            (activity as AppActivity?)?.supportActionBar?.title = it.name
//            binding.name.text = it.name
//            binding.avatar.loadCircleCrop(it.avatar)
//        }

        val postAdapter = PostAdapter(object : PostInteractionListener {
            override fun onLike(post: PostResponse) {
                if (authViewModel.authenticated) {
                    if (!post.likedByMe) postViewModel.likePostById(post.id) else postViewModel.dislikePostById(post.id)
                } else {
                    Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.list_post)
                }
            }

            override fun onEdit(post: PostResponse) {
                findNavController().navigate(
                    R.id.newPostFragment,
                    Bundle().apply { intArg = post.id })
            }

            override fun onRemove(post: PostResponse) {
                postViewModel.removePostById(post.id)
            }

            override fun onShare(post: PostResponse) {
                if (authViewModel.authenticated) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }

                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.share_description))
                    startActivity(shareIntent)
                } else {
                    Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.list_post)
                }
            }

            override fun loadLikedAndMentionedUsersList(post: PostResponse) {
                if (authViewModel.authenticated) {
                    if (post.users.values.isEmpty()) {
                        return
                    } else {
                        postViewModel.getLikedAndMentionedUsersList(post)
                        findNavController().navigate(R.id.list_post)
                    }
                } else {
                    Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.list_post)
                }
            }

            override fun onShowPhoto(post: PostResponse) {
                if (post.attachment?.url != "") {
                    when (post.attachment?.type) {
                        AttachmentType.IMAGE -> {
                            findNavController().navigate(R.id.list_post,
                                Bundle().apply { textArg = post.attachment.url })
                        }
                        else -> return
                    }
                }
            }
        })

//        binding.postList.adapter = postAdapter.withLoadStateHeaderAndFooter(
//            header = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
//                override fun onRetry() {
//                    postAdapter.retry()
//                }
//            }),
//            footer = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
//                override fun onRetry() {
//                    postAdapter.retry()
//                }
//            }),
//        )
//
//        lifecycleScope.launchWhenCreated {
//            println(userProfileViewModel.postData.toString())
//            userProfileViewModel.postData.collectLatest(postAdapter::submitData)
//        }

        binding.addJob.setOnClickListener {
            findNavController().navigate(R.id.list_post)
        }

        binding.addPost.setOnClickListener {
            findNavController().navigate(R.id.newPostFragment)
        }

        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}