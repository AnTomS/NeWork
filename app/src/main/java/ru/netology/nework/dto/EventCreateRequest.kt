package ru.netology.nework.dto

import ru.netology.nework.enumiration.EventType

data class EventCreateRequest(
    val id: Int = 0,
    val content: String = "",
    val datetime: String? = null,
    val type: EventType? = EventType.OFFLINE,
    val attachment: Attachment? = null,
    val link: String? = null,
    val speakerIds: List<Int>? = null,
)
