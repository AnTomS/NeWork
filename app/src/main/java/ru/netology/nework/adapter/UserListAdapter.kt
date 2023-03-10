package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.CardUserInListBinding
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.view.loadCircleCrop

interface UsersListInteractionListener {
    fun openUserProfile(id: Int)
}

class UsersListAdapter(private val onInteractionListener: UsersListInteractionListener) :
    ListAdapter<UserPreview, UsersListViewHolder>(UserPreviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersListViewHolder {
        val binding =
            CardUserInListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsersListViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: UsersListViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class UsersListViewHolder(
    private val binding: CardUserInListBinding,
    private val listener: UsersListInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: UserPreview) {
        binding.apply {
            avatar.loadCircleCrop(user.avatar)
            avatar.setOnClickListener {
                listener.openUserProfile(user.id)
            }
            author.setOnClickListener {
                listener.openUserProfile(user.id)
            }
            author.text = user.name

        }
    }
}

class UserPreviewDiffCallback : DiffUtil.ItemCallback<UserPreview>() {
    override fun areItemsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
        return oldItem == newItem
    }
}