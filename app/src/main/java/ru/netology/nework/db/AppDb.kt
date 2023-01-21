package ru.netology.nework.db


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.dao.*
import ru.netology.nework.entity.*

@Database(
    entities = [PostEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
        PostRemoteKeyEntity::class,
        UserEntity::class,
        JobEntity::class,
    ], version = 5
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao

}

