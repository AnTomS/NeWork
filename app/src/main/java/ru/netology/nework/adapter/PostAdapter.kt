package ru.netology.nework.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.MediaItem
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.PostResponse
import ru.netology.nework.enumiration.AttachmentType
import ru.netology.nework.utils.Utils
import ru.netology.nework.view.load

import ru.netology.nework.view.loadCircleCrop


interface PostInteractionListener {
    fun onLike(post: PostResponse) {}
    fun onEdit(post: PostResponse) {}
    fun onRemove(post: PostResponse) {}
    fun onShare(post: PostResponse) {}
    fun loadLikedAndMentionedUsersList(post: PostResponse) {}
    fun onShowPhoto(post: PostResponse) {}
}

class PostAdapter(
    private val interactionListener: PostInteractionListener,
) : PagingDataAdapter<PostResponse, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, interactionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val listener: PostInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    private val parentView = binding.root
    val videoThumbnail = binding.videoThumbnail
    val videoContainer = binding.videoContainer
    val videoProgressBar = binding.videoProgressBar
    var videoPreview: MediaItem? = null
    val videoPlayIcon: ImageView = binding.videoButton

    fun bind(post: PostResponse) {
        parentView.tag = this
        binding.apply {
            if (!post.mentionedMe) mentionedMe.visibility = View.GONE else mentionedMe.visibility =
                View.VISIBLE

            avatar.loadCircleCrop(post.authorAvatar)

            if (post.attachment?.url != "") {
                when (post.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        videoPreview = null
                        image.visibility = View.VISIBLE
                        videoContainer.visibility = View.GONE
                        image.load(post.attachment.url)
                    }
                    AttachmentType.VIDEO -> {
                        image.visibility = View.GONE
                        videoContainer.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(post.attachment.url)
                        videoThumbnail.load(post.attachment.url)
                    }
                    AttachmentType.AUDIO -> {
                        image.visibility = View.GONE
                        videoContainer.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(post.attachment.url)
                        videoThumbnail.setImageDrawable(
                            AppCompatResources.getDrawable(
                                itemView.context,
                                R.drawable.ic_audiotrack_24
                            )
                        )
                    }
                    null -> {
                        videoContainer.visibility = View.GONE
                        image.visibility = View.GONE
                        videoPreview = null
                    }
                }
            }
            author.text = post.author
            published.text = Utils.convertDateAndTime(post.published)
            val linkText = if (post.link != null) {
                "\n" + post.link
            } else {
                ""
            }
            val postText = post.content + linkText
            content.text = postText
            like.isChecked = post.likedByMe
            like.text = "${post.likeOwnerIds.size}"
            mentionedMe.visibility = if (post.mentionedMe) View.VISIBLE else View.INVISIBLE
            menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE
            mention.text = "${post.mentionIds.size}"

            if (post.users.isEmpty()) {
                postUsersGroup.visibility = View.GONE
            } else {
                val firstUserAvatarUrl = post.users.values.first().avatar
                avatar.loadCircleCrop(firstUserAvatarUrl)
                postUsersGroup.visibility = View.VISIBLE
                if (post.users.size >= 2) {
                    val likedAndMentionedUsersText =
                        "${post.users.values.first().name} and ${post.users.size - 1} users"
                    likedAndMentionedUsers.text = likedAndMentionedUsersText
                } else if (post.users.size == 1) {
                    likedAndMentionedUsers.text = post.users.values.first().name
                }
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                listener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                listener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                listener.onLike(post)
            }

            share.setOnClickListener {
                listener.onShare(post)
            }

            image.setOnClickListener {
                listener.onShowPhoto(post)
            }

            postUsersGroup.setOnClickListener {
                listener.loadLikedAndMentionedUsersList(post)
            }

        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<PostResponse>() {
    override fun areItemsTheSame(oldItem: PostResponse, newItem: PostResponse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PostResponse, newItem: PostResponse): Boolean {
        return oldItem == newItem
    }
}