package ru.netology.nework.model

import ru.netology.nework.dto.PostResponse

data class FeedModel(
    val posts: List<PostResponse> = emptyList(),
    val empty: Boolean = false,
)