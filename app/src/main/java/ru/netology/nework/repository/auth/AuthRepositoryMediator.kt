package ru.netology.nework.repository.auth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.repository.auth.AuthRepository
import ru.netology.nework.repository.auth.AuthRepositoryImp
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
interface AuthRepositoryModule {
    @Binds
    @Singleton
    fun bindAuthRepository(impl: AuthRepositoryImp): AuthRepository
}
