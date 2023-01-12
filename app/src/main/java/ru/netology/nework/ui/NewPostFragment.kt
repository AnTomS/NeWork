package ru.netology.nework.ui


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.utils.StringArg
import ru.netology.nework.viewmodel.PostViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewPostFragment: Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.textArg
            ?.let(binding.edit::setText)


        viewModel.postCreated.observe(viewLifecycleOwner) {
            //viewModel.loadPosts()
            findNavController().navigateUp()
        }
        return binding.root
    }
}