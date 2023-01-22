package ru.netology.nework.dao

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.enumiration.AttachmentType
import ru.netology.nework.enumiration.EventType

class Converters {
    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name

    @TypeConverter
    fun fromListInt(list: List<Int?>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toListInt(string: String?): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(string, type)
    }

    @TypeConverter
    fun fromUsers(map: Map<Int, UserPreview>): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toUsers(string: String): Map<Int, UserPreview> {
        val maptype = object : TypeToken<Map<Int, UserPreview>>() {}.type
        return Gson().fromJson(string, maptype)
    }

    @TypeConverter
    fun fromEventType(value: EventType) = value.name

    @TypeConverter
    fun toEventType(value: String) = enumValueOf<EventType>(value)

}