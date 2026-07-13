package com.example.geofencing.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.geofencing.data.local.violationAckDataStore
import com.example.geofencing.data.repository.GeofenceEventRepository
import com.example.geofencing.data.repository.GeofenceEventRepositoryImpl
import com.example.geofencing.data.repository.SectorRepository
import com.example.geofencing.data.repository.SectorRepositoryImpl
import com.example.geofencing.data.repository.SiteRepository
import com.example.geofencing.data.repository.SiteRepositoryImpl
import com.example.geofencing.data.repository.ViolationAckRepository
import com.example.geofencing.data.repository.ViolationAckRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Binds
    @Singleton
    abstract fun bindViolationAckRepository(
        impl: ViolationAckRepositoryImpl
    ): ViolationAckRepository

    companion object {
        @Provides
        @Singleton
        fun provideViolationAckDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.violationAckDataStore
    }
}
