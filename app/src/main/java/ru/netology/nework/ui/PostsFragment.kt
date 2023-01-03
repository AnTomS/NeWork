package ru.netology.nework.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.netology.nework.R
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.adapter.PostsAdapter
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.viewmodel.PostViewModel

import kotlin.system.exitProcess

class PostsFragment : Fragment() {

    internal val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostsBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

        })
        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)

            binding.emptyText.isVisible = state.empty
        }


        // Поскольку выполнение DiffUtil происходит асинхронно,
        // нужно подписаться на момент вставки в адаптер,
        // а не скроллить сразу по клику на newPostFab
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        })


        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .setAction(R.string.close_app) {
                        activity?.finish()
                        exitProcess(0)
                    }
                    .show()
            }
        }

        binding.newPostFab.visibility = View.GONE


        viewModel.newerCount.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.newPostFab.text = getString(R.string.new_post)
                binding.newPostFab.visibility = View.VISIBLE
            }
            println("Newer count: $it")
        }

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.newPostFab.setOnClickListener {
            binding.newPostFab.visibility = View.GONE
            viewModel.readNewPosts()

        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_list_post_to_newPostFragment)
        }

        return binding.root
    }
}
