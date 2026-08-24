package com.example.geofencing.ui.mock

import com.example.geofencing.ui.common.LoadState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// 새 대시보드 화면(WholeSector/Sector/Cart)이 쓰는 데이터 소스. ViewModel은 이 인터페이스에만 의존한다.
// LoadState를 함께 실어 로딩/에러/성공을 표현한다 → 구현체가 캐시를 쥐고 있어도(예: Supabase)
// 에러 후 refresh()로 재시도가 가능하다. 지금은 SupabaseDashboardRepository(임시 실백엔드).
interface DashboardRepository {
    fun observeSectors(): Flow<LoadState<List<MockSector>>>
    fun observeSector(name: String): Flow<LoadState<MockSector?>>
    fun observeCart(sectorName: String, cartId: String): Flow<LoadState<MockCart?>>

    // 소스를 다시 불러온다(에러 재시도/수동 새로고침).
    fun refresh()
}

// MockSectors를 그대로 흘려보내는 구현(항상 Success). 실패가 없으므로 refresh는 no-op.
@Singleton
class MockDashboardRepository @Inject constructor() : DashboardRepository {
    private val sectors = MutableStateFlow(MockSectors)

    override fun observeSectors(): Flow<LoadState<List<MockSector>>> =
        sectors.map { LoadState.Success(it) }

    override fun observeSector(name: String): Flow<LoadState<MockSector?>> =
        sectors.map { list -> LoadState.Success(list.find { it.name == name }) }

    override fun observeCart(sectorName: String, cartId: String): Flow<LoadState<MockCart?>> =
        sectors.map { list ->
            LoadState.Success(list.find { it.name == sectorName }?.carts?.find { it.id == cartId })
        }

    override fun refresh() = Unit
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardRepositoryModule {
    // Supabase(임시 실백엔드) 구현으로 바인딩. mock으로 되돌리려면 MockDashboardRepository로 교체.
    @Binds
    abstract fun bindDashboardRepository(impl: SupabaseDashboardRepository): DashboardRepository
}
