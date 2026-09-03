package com.example.geofencing.ui.dashboard

import com.example.geofencing.ui.common.LoadState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Data source used by the dashboard flow (WholeSector, Sector, Cart).
// ViewModels depend on this interface, while implementations may use sample data or Supabase.
interface DashboardRepository {
    fun observeSectors(): Flow<LoadState<List<DashboardSector>>>
    fun observeSector(name: String): Flow<LoadState<DashboardSector?>>
    fun observeCart(sectorName: String, cartId: String): Flow<LoadState<DashboardCart?>>

    fun refresh()
}

@Singleton
class MockDashboardRepository @Inject constructor() : DashboardRepository {
    private val sectors = MutableStateFlow(SampleDashboardSectors)

    override fun observeSectors(): Flow<LoadState<List<DashboardSector>>> =
        sectors.map { LoadState.Success(it) }

    override fun observeSector(name: String): Flow<LoadState<DashboardSector?>> =
        sectors.map { list -> LoadState.Success(list.find { it.name == name }) }

    override fun observeCart(sectorName: String, cartId: String): Flow<LoadState<DashboardCart?>> =
        sectors.map { list ->
            LoadState.Success(list.find { it.name == sectorName }?.carts?.find { it.id == cartId })
        }

    override fun refresh() = Unit
}

@Module
@InstallIn(SingletonComponent::class)
object DashboardRepositoryModule {
    @Provides
    fun provideDashboardRepository(impl: SupabaseDashboardRepository): DashboardRepository = impl
}
