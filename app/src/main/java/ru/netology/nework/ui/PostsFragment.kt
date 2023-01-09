package ru.netology.nework.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.AppActivity
import ru.netology.nework.R
import ru.netology.nework.adapter.PagingLoadStateAdapter
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.adapter.PostInteractionListener
import ru.netology.nework.adapter.PostRecyclerView
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.PostResponse
import ru.netology.nework.enumiration.AttachmentType
import ru.netology.nework.utils.IntArg
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PostsFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    lateinit var mediaRecyclerView: PostRecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostsBinding.inflate(inflater, container, false)

        (activity as AppActivity).supportActionBar?.title = getString(R.string.list_of_post)

        mediaRecyclerView = binding.list

        val adapter = PostAdapter(object : PostInteractionListener {
            override fun onLike(post: PostResponse) {
                if (authViewModel.authenticated) {
                    if (!post.likedByMe) viewModel.likePostById(post.id) else viewModel.dislikePostById(
                        post.id
                    )
                } else {
                    Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_SHORT)
                        .show()
                   // findNavController().navigate(R.id.action_postFeedFragment_to_signInFragment)
                }
            }

            override fun onEdit(post: PostResponse) {
                findNavController().navigate(
                    R.id.action_list_post_to_newPostFragment,
                    Bundle().apply { intArg = post.id })
            }

            override fun onRemove(post: PostResponse) {
                viewModel.removePostById(post.id)
            }

            override fun onShare(post: PostResponse) {
                if (authViewModel.authenticated) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }
                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.description_post_share))
                    startActivity(shareIntent)
                } else {
                    Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_SHORT)
                        .show()
                    //findNavController().navigate(R.id.action_postFeedFragment_to_signInFragment)
                }
            }

            override fun loadLikedAndMentionedUsersList(post: PostResponse) {
                if (authViewModel.authenticated) {
                    if (post.users.values.isEmpty()) {
                        return
                    } else {
                        viewModel.getLikedAndMentionedUsersList(post)
                       // findNavController().navigate(R.id.action_postFeedFragment_to_postUsersListFragment)
                    }
                } else {
                    Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_SHORT)
                        .show()
                    //findNavController().navigate(R.id.action_postFeedFragment_to_signInFragment)
                }
            }

            override fun onShowPhoto(post: PostResponse) {
                if (post.attachment?.url != "") {
                    when (post.attachment?.type) {
                        AttachmentType.IMAGE -> {
                           // findNavController().navigate(R.id.action_postFeedFragment_to_showPhotoFragment,
                              //  Bundle().apply { textArg = post.attachment.url })
                        }
                        else -> return
                    }
                }
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
            footer = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
        )

        binding.list.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                    .show()
            }
            if (state.loading) {
                Snackbar.make(binding.root, R.string.server_error_message, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest(adapter::submitData)
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        })

        adapter.loadStateFlow
        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing = it.refresh is LoadState.Loading
            }
        }


        return binding.root
    }

    companion object {
        var Bundle.intArg: Int by IntArg
    }

    override fun onResume() {
        if (::mediaRecyclerView.isInitialized) mediaRecyclerView.createPlayer()
        super.onResume()
    }

    override fun onPause() {
        if (::mediaRecyclerView.isInitialized) mediaRecyclerView.releasePlayer()
        super.onPause()
    }


    override fun onStop() {
        if (::mediaRecyclerView.isInitialized) mediaRecyclerView.releasePlayer()
        super.onStop()
    }

}


