package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nework.dao.Converters
import ru.netology.nework.dto.EventResponse
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.enumiration.EventType

@Entity
@TypeConverters(Converters::class)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val type: EventType,
    val likeOwnerIds: List<Int>,
    val likedByMe: Boolean,
    val speakerIds: List<Int>,
    val participantsIds: List<Int>,
    val participatedByMe: Boolean,
    @Embedded
    val attachment: AttachmentEmbedded?,
    val link: String?,
    val ownedByMe: Boolean,
    val users: Map<Int, UserPreview>,
) {

    fun toDto() = EventResponse(
        id, authorId, author, authorAvatar, authorJob, content,
        datetime, published, type, likeOwnerIds, likedByMe, speakerIds,
        participantsIds, participatedByMe, attachment?.toDto(), link, ownedByMe, users
    )

    companion object {
        fun fromDto(dto: EventResponse) =
            EventEntity(
                dto.id, dto.authorId, dto.author, dto.authorAvatar, dto.authorJob,
                dto.content, dto.datetime, dto.published,
                dto.type, dto.likeOwnerIds, dto.likedByMe,
                dto.speakerIds, dto.participantsIds,
                dto.participatedByMe, AttachmentEmbedded.fromDto(dto.attachment),
                dto.link, dto.ownedByMe, dto.users
            )
    }
}

fun List<EventResponse>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)