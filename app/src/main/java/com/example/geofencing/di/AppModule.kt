package com.example.geofencing.di

import com.example.geofencing.data.repository.GeofenceRepository
import com.example.geofencing.data.repository.GeofenceRepositoryImpl
import com.example.geofencing.data.repository.SectorRepository
import com.example.geofencing.data.repository.SectorRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindGeofenceRepository(
        impl: GeofenceRepositoryImpl
    ): GeofenceRepository

    @Binds
    @Singleton
    abstract fun bindSectorRepository(
        impl: SectorRepositoryImpl
    ): SectorRepository
}
