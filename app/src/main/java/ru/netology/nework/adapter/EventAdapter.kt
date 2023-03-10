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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.EventResponse
import ru.netology.nework.enumiration.AttachmentType
import ru.netology.nework.enumiration.EventType
import ru.netology.nework.utils.Utils
import ru.netology.nework.view.load
import ru.netology.nework.view.loadCircleCrop

interface EventInteractionListener {
    fun onLike(event: EventResponse) {}
    fun onShare(event: EventResponse) {}
    fun onParticipateInEvent(event: EventResponse) {}
    fun onEdit(event: EventResponse) {}
    fun onRemove(event: EventResponse) {}
    fun loadEventUsersList(event: EventResponse) {}
    fun onShowPhoto(event: EventResponse) {}
}

class EventAdapter(
    private val onInteractionListener: EventInteractionListener,
) : PagingDataAdapter<EventResponse, EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position) ?: return
        holder.bind(event)
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val listener: EventInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    private val parentView = binding.root
    val videoThumbnail = binding.videoThumbnail
    val videoContainer = binding.videoContainer
    val videoProgressBar = binding.videoProgressBar
    var videoPreview: MediaItem? = null
    val videoPlayIcon: ImageView = binding.videoButton

    @OptIn(ExperimentalCoroutinesApi::class)
    fun bind(event: EventResponse) {
        parentView.tag = this
        binding.apply {
            when (event.type) {
                EventType.OFFLINE -> type.setImageResource(R.drawable.ic_offline_24)
                EventType.ONLINE -> type.setImageResource(R.drawable.ic_online_24)
            }

            avatar.loadCircleCrop(event.authorAvatar)

            if (event.attachment?.url != "") {
                when (event.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        videoPreview = null
                        image.visibility = View.VISIBLE
                        videoContainer.visibility = View.GONE
                        image.load(event.attachment.url)
                    }
                    AttachmentType.VIDEO -> {
                        image.visibility = View.GONE
                        videoContainer.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(event.attachment.url)
                        videoThumbnail.load(event.attachment.url)
                    }
                    AttachmentType.AUDIO -> {
                        image.visibility = View.GONE
                        videoContainer.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(event.attachment.url)
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

            author.text = event.author
            published.text = Utils.convertDateAndTime(event.published)
            dateTime.text = Utils.convertDateAndTime(event.datetime)
            val linkText = if (event.link != null) {
                "\n" + event.link
            } else {
                ""
            }
            val eventText = event.content + linkText
            content.text = eventText
            like.isChecked = event.likedByMe
            like.text = "${event.likeOwnerIds.size}"
            participate.isChecked = event.participatedByMe
            participate.text = "${event.participantsIds.size}"
            menu.visibility = if (event.ownedByMe) View.VISIBLE else View.INVISIBLE
            speakers.text = "${event.speakerIds.size}"

            if (event.users.isEmpty()) {
                eventUsersGroup.visibility = View.GONE
            } else {
                val firstUserAvatarUrl = event.users.values.first().avatar
                avatar.loadCircleCrop(firstUserAvatarUrl)
                eventUsersGroup.visibility = View.VISIBLE
                if (event.users.size >= 2) {
                    val likedAndMentionedUsersText =
                        "${event.users.values.first().name} and ${event.users.size - 1} users"
                    eventUsers.text = likedAndMentionedUsersText
                } else if (event.users.size == 1) {
                    eventUsers.text = event.users.values.first().name
                }
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                listener.onRemove(event)
                                true
                            }
                            R.id.edit -> {
                                listener.onEdit(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                listener.onLike(event)
            }
            share.setOnClickListener {
                listener.onShare(event)
            }
            participate.setOnClickListener {
                listener.onParticipateInEvent(event)
            }

            image.setOnClickListener {
                listener.onShowPhoto(event)
            }

            eventUsersGroup.setOnClickListener {
                listener.loadEventUsersList(event)
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<EventResponse>() {
    override fun areItemsTheSame(oldItem: EventResponse, newItem: EventResponse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: EventResponse, newItem: EventResponse): Boolean {
        return oldItem == newItem
    }
}