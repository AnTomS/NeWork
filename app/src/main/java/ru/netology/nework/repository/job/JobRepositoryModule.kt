package ru.netology.nework.repository.job

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface JobRepositoryModule {
    @Binds
    @Singleton
    fun bindJobRepository(impl: JobRepositoryImpl): JobRepository
}