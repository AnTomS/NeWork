package ru.netology.nework.dto

import ru.netology.nework.enumiration.AttachmentType

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String = "",
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val viewed: Boolean = false
)

data class Attachment(
    val url: String,
    val description: String?,
    val type: AttachmentType,
)