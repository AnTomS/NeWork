package ru.netology.nework.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.adapter.EventAdapter
import ru.netology.nework.adapter.EventInteractionListener
import ru.netology.nework.adapter.EventRecyclerView
import ru.netology.nework.adapter.PagingLoadStateAdapter
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.dto.EventResponse
import ru.netology.nework.enumiration.AttachmentType
import ru.netology.nework.ui.UserProfileFragment.Companion.textArg
import ru.netology.nework.utils.IntArg
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EventsFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private val viewModel: EventViewModel by activityViewModels()
    lateinit var mediaRecyclerView: EventRecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventsBinding.inflate(inflater, container, false)

        (activity as AppActivity).supportActionBar?.title = getString(R.string.events)

        authViewModel.data.observeForever {
            if (!authViewModel.authenticated) {
                binding.fab.visibility = View.GONE
            } else {
                binding.fab.visibility = View.VISIBLE
            }
        }

        mediaRecyclerView = binding.list

        val adapter = EventAdapter(object : EventInteractionListener {
            override fun onLike(event: EventResponse) {
                if (authViewModel.authenticated) {
                    if (!event.likedByMe) viewModel.likeEventById(event.id) else viewModel.dislikeEventById(
                        event.id
                    )
                } else {
                    Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_events_to_signInFragment)
                }
            }

            override fun onShare(event: EventResponse) {
                if (authViewModel.authenticated) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, event.content)
                        type = "text/plain"
                    }
                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.share_description))
                    startActivity(shareIntent)
                } else {
                    Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_events_to_signInFragment)
                }
            }

            override fun onEdit(event: EventResponse) {
                findNavController().navigate(
                    R.id.action_list_post_to_newPostFragment,
                    Bundle().apply { intArg = event.id })
            }

            override fun onRemove(event: EventResponse) {
                viewModel.removeEventById(event.id)
            }

            override fun loadEventUsersList(event: EventResponse) {
                if (authViewModel.authenticated) {
                    if (event.speakerIds.isEmpty()) {
                        return
                    } else {
                        viewModel.getEventUsersList(event)
                        findNavController().navigate(R.id.action_events_to_event_users)
                    }
                } else {
                    Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_events_to_signInFragment)
                }
            }

            override fun onParticipateInEvent(event: EventResponse) {
                if (authViewModel.authenticated) {
                    if (!event.participatedByMe) viewModel.participateInEvent(event.id) else viewModel.quitParticipateInEvent(
                        event.id
                    )
                } else {
                    Snackbar.make(binding.root, R.string.log_in_to_continue, Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_events_to_signInFragment)
                }
            }

            override fun onShowPhoto(event: EventResponse) {
                if (event.attachment?.url != "") {
                    when (event.attachment?.type) {
                        AttachmentType.IMAGE -> {
                            findNavController().navigate(R.id.action_list_post_to_newPostFragment,
                                Bundle().apply { textArg = event.attachment.url })
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

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_list_post_to_newPostFragment)
        }

        return binding.root
    }

    companion object {
        var Bundle.intArg: Int by IntArg
    }

    override fun onResume() {
        if(::mediaRecyclerView.isInitialized) mediaRecyclerView.createPlayer()
        super.onResume()
    }

    override fun onPause() {
        if(::mediaRecyclerView.isInitialized) mediaRecyclerView.releasePlayer()
        super.onPause()
    }

    override fun onStop() {
        if(::mediaRecyclerView.isInitialized) mediaRecyclerView.releasePlayer()
        super.onStop()
    }
}