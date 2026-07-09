package com.example.geofencing.di

import com.example.geofencing.data.repository.GeofenceEventRepository
import com.example.geofencing.data.repository.GeofenceEventRepositoryImpl
import com.example.geofencing.data.repository.SectorRepository
import com.example.geofencing.data.repository.SectorRepositoryImpl
import com.example.geofencing.data.repository.SiteRepository
import com.example.geofencing.data.repository.SiteRepositoryImpl
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
    abstract fun bindSectorRepository(
        impl: SectorRepositoryImpl
    ): SectorRepository

    @Binds
    @Singleton
    abstract fun bindSiteRepository(
        impl: SiteRepositoryImpl
    ): SiteRepository

    @Binds
    @Singleton
    abstract fun bindGeofenceEventRepository(
        impl: GeofenceEventRepositoryImpl
    ): GeofenceEventRepository
}
