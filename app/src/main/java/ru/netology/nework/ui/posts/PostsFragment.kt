package ru.netology.nework.ui.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.netology.nework.databinding.FragmentPostsBinding

class PostsFragment : Fragment() {

    private var _binding: FragmentPostsBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val postsViewModel =
            ViewModelProvider(this).get(PostsViewModel::class.java)

        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textPosts
        postsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}