package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nework.dao.Converters
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates

import ru.netology.nework.dto.PostResponse
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.enumiration.AttachmentType

@Entity
@TypeConverters(Converters::class)
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    val coords: Coordinates?,
    val link: String?,
    val likeOwnerIds: List<Int>,
    val mentionIds: List<Int>,
    val mentionedMe: Boolean,
    val likedByMe: Boolean,
    @Embedded
    val attachment: AttachmentEmbedded?,
    val ownedByMe: Boolean,
    val users: Map<Int, UserPreview>
) {
    fun toDto() = PostResponse(
        id, authorId, author, authorAvatar, authorJob, content,
        published, coords, link, likeOwnerIds, mentionIds, mentionedMe,
        likedByMe, attachment?.toDto(), ownedByMe, users
    )

    companion object {
        fun fromDto(dto: PostResponse) =
            PostEntity(
                dto.id, dto.authorId, dto.author, dto.authorAvatar, dto.authorJob,
                dto.content, dto.published, dto.coords,
                dto.link, dto.likeOwnerIds, dto.mentionIds, dto.mentionedMe,
                dto.likedByMe, AttachmentEmbedded.fromDto(dto.attachment), dto.ownedByMe, dto.users
            )
    }
}

data class AttachmentEmbedded(
    var url: String,
    var typeAttach: AttachmentType,
) {
    fun toDto() = Attachment(url, typeAttach)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbedded(it.url, it.type)
        }
    }
}

fun List<PostResponse>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)