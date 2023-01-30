package ru.netology.nework.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentRegisterBinding
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.repository.auth.AuthRepository
import ru.netology.nework.utils.Utils
import ru.netology.nework.viewmodel.RegisterViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    @Inject
    lateinit var auth: AppAuth

    @Inject
    lateinit var repository: AuthRepository

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentRegisterBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.fragment_register)
        binding.addAvatar.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_add_avatar)
        }

        binding.singButton.setOnClickListener {
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()
            val name = binding.username.text.toString()

            when {
                binding.login.text.isNullOrBlank() || binding.password.text.isNullOrBlank() -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.error_filling_forms),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                binding.confirmPassword.text.toString() != pass -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.password_doesnt_match),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                viewModel.avatar.value?.file == null -> {
                    viewModel.register(login, pass, name)
                    Utils.hideKeyboard(requireView())
                    findNavController().navigate(R.id.profile)
                }
                else -> {
                    val file = viewModel.avatar.value?.file?.let { MediaUpload(it) }
                    file?.let { viewModel.registerWithPhoto(login, pass, name, it) }
                    Utils.hideKeyboard(requireView())
                    findNavController().navigate(R.id.profile)
                }
            }
        }
        return binding.root
    }
}

