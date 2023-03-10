package ru.netology.nework.ui


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.R
import ru.netology.nework.adapter.CreatePageUsersListAdapter
import ru.netology.nework.adapter.CreatePageUsersListInteractionListener
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.enumiration.AttachmentType
import ru.netology.nework.ui.PostsFragment.Companion.intArg
import ru.netology.nework.ui.UserProfileFragment.Companion.textArg
import ru.netology.nework.utils.Utils
import ru.netology.nework.viewmodel.PostViewModel
import java.io.File

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewPostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.create_post)

        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        val viewModel: PostViewModel by activityViewModels()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Snackbar.make(binding.root, R.string.skip_edit_question, Snackbar.LENGTH_SHORT)
                .setAction(R.string.exit) {
                    viewModel.deleteEditPost()
                    findNavController().navigate(R.id.list_post)
                }.show()
        }


        if (arguments?.intArg != null) {
            val id = arguments?.intArg
            id?.let { viewModel.getPostCreateRequest(it) }
        }

        val adapter = CreatePageUsersListAdapter(object : CreatePageUsersListInteractionListener {
            override fun openUserProfile(id: Int) {
                val idAuthor = id.toString()
                findNavController().navigate(
                    R.id.profile,
                    Bundle().apply { textArg = idAuthor })
            }

            override fun deleteFromList(id: Int) {
                viewModel.unCheck(id)
                viewModel.addMentionIds()
            }
        })

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        val resultFile = uri?.toFile()
                        val file = MultipartBody.Part.createFormData(
                            "file", resultFile?.name, resultFile!!.asRequestBody()
                        )
                        viewModel.changeMedia(uri, resultFile, AttachmentType.IMAGE)
                        viewModel.addMediaToPost(AttachmentType.IMAGE, file)
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                        "image/jpg"
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        val pickVideoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val resultCode = activityResult.resultCode
                val data = activityResult.data

                if (resultCode == Activity.RESULT_OK) {
                    val selectedVideoUri = data?.data!!
                    val selectedVideoPath =
                        Utils.getVideoPathFromUri(selectedVideoUri, requireActivity())
                    if (selectedVideoPath != null) {
                        val resultFile = File(selectedVideoPath)
                        val file = MultipartBody.Part.createFormData(
                            "file", resultFile.name, resultFile.asRequestBody()
                        )
                        viewModel.changeMedia(selectedVideoUri, resultFile, AttachmentType.VIDEO)
                        viewModel.addMediaToPost(AttachmentType.VIDEO, file)
                    }
                } else {
                    Snackbar.make(binding.root, R.string.video_container, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }

        binding.pickVideo.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            pickVideoLauncher.launch(intent)
        }

        val pickAudioLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val resultCode = activityResult.resultCode
                val data = activityResult.data

                if (resultCode == Activity.RESULT_OK) {
                    val selectedAudioUri = data?.data!!
                    val selectedAudioPath =
                        Utils.getAudioPathFromUri(selectedAudioUri, requireActivity())
                    if (selectedAudioPath != null) {
                        val resultFile = File(selectedAudioPath)
                        val file = MultipartBody.Part.createFormData(
                            "file", resultFile.name, resultFile.asRequestBody()
                        )
                        viewModel.changeMedia(selectedAudioUri, resultFile, AttachmentType.AUDIO)
                        viewModel.addMediaToPost(AttachmentType.AUDIO, file)
                    }
                }
            }

        binding.pickAudio.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            )
            pickAudioLauncher.launch(intent)
        }

        viewModel.media.observe(viewLifecycleOwner)
        { mediaModel ->
            if (mediaModel.uri == null) {
                binding.mediaContainer.visibility = View.GONE
                return@observe
            }
            when (mediaModel.type) {
                AttachmentType.IMAGE -> {
                    binding.mediaContainer.visibility = View.VISIBLE
                    binding.image.setImageURI(mediaModel.uri)
                }
                AttachmentType.VIDEO -> {
                    binding.mediaContainer.visibility = View.VISIBLE
                    binding.image.setImageResource(R.drawable.ic_sample_video_24)
                }
                AttachmentType.AUDIO -> {
                    binding.mediaContainer.visibility = View.VISIBLE
                    binding.image.setImageResource(R.drawable.ic_audio_24)
                }
                null -> return@observe
            }
        }

        binding.addMention.setOnClickListener {
            findNavController().navigate(R.id.action_new_post_to_chooseEventUsersFragment)
        }
        binding.addLink.setOnClickListener {
            val link: String = binding.link.text.toString()
            viewModel.addLink(link)
        }

        binding.mentionIds.adapter = adapter
        viewModel.mentionsData.observe(viewLifecycleOwner)
        {
            if (it.isEmpty()) {
                binding.scrollMentions.visibility = View.GONE
            } else {
                adapter.submitList(it)
                binding.scrollMentions.visibility = View.VISIBLE
            }
        }

        viewModel.newPost.observe(viewLifecycleOwner)
        {
            it.content.let(binding.edit::setText)
            it.link.let(binding.addLink::setText)
            if (it.attachment != null) {
                binding.mediaContainer.visibility = View.VISIBLE
            } else {
                binding.mediaContainer.visibility = View.GONE
            }
        }

        binding.removeMedia.setOnClickListener {
            viewModel.changeMedia(null, null, null)
            viewModel.newPost.value = viewModel.newPost.value?.copy(attachment = null)
            binding.mediaContainer.visibility = View.GONE
        }

        binding.save.setOnClickListener {
            val content = binding.edit.text.toString()
            if (content == "") {
                Snackbar.make(binding.root, R.string.fill_text, Snackbar.LENGTH_SHORT).show()
            } else {
                viewModel.savePost(content)
            }

            viewModel.postCreated.observe(viewLifecycleOwner) {
                findNavController().navigate(R.id.list_post)
            }

            viewModel.dataState.observe(viewLifecycleOwner) { state ->
                if (state.error) {
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
        return binding.root
    }
}