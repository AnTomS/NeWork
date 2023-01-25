package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentShowImageBinding
import ru.netology.nework.utils.StringArg
import ru.netology.nework.view.load

class ShowImageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentShowImageBinding.inflate(
            inflater,
            container,
            false
        )
        (activity as AppActivity).supportActionBar?.title = getString(R.string.photo)
        val url = arguments?.textArg
        binding.imageView.load(url)

        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}
