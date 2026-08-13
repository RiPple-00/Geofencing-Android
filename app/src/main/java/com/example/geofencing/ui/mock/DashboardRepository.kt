package com.example.geofencing.ui.mock

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
// 지금은 MockDashboardRepository(정적 mock), 백엔드 API 나오면 구현체만 교체(+ DTO 매핑).
// (기존 data.repository.SectorRepository와는 다른 화면·도메인이라 이름을 분리했다.)
interface DashboardRepository {
    fun observeSectors(): Flow<List<MockSector>>
    fun observeSector(name: String): Flow<MockSector?>
    fun observeCart(sectorName: String, cartId: String): Flow<MockCart?>
}

// MockSectors를 그대로 흘려보내는 구현. Flow라 나중에 실시간 갱신(카트 이동 등) 시뮬레이션도 가능.
@Singleton
class MockDashboardRepository @Inject constructor() : DashboardRepository {
    private val sectors = MutableStateFlow(MockSectors)

    override fun observeSectors(): Flow<List<MockSector>> = sectors

    override fun observeSector(name: String): Flow<MockSector?> =
        sectors.map { list -> list.find { it.name == name } }

    override fun observeCart(sectorName: String, cartId: String): Flow<MockCart?> =
        sectors.map { list -> list.find { it.name == sectorName }?.carts?.find { it.id == cartId } }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardRepositoryModule {
    @Binds
    abstract fun bindDashboardRepository(impl: MockDashboardRepository): DashboardRepository
}
