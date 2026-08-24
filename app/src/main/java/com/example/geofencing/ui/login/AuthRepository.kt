package com.example.geofencing.ui.login

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

// 로그인 인증의 진입점. UI/ViewModel은 이 인터페이스에만 의존하고, 구현체(스텁/Supabase Auth 등)만 교체한다.
// (DashboardRepository의 mock↔Supabase 교체 패턴과 동일.)
interface AuthRepository {
    // 성공하면 Result.success, 실패하면 Result.failure(사람이 읽을 메시지 포함).
    suspend fun login(id: String, password: String): Result<Unit>
}

// 임시 스텁 — 실제 백엔드 인증 붙기 전 흐름을 완성하기 위한 구현.
// 데모 계정(deltax / deltax)만 통과시켜 성공/실패 경로를 모두 확인할 수 있게 한다.
// TODO(인증): Supabase Auth(POST /auth/v1/token) 등 실제 구현체로 교체.
@Singleton
class StubAuthRepository @Inject constructor() : AuthRepository {
    override suspend fun login(id: String, password: String): Result<Unit> {
        delay(600) // 네트워크 지연 흉내(로딩 상태 확인용)
        return when {
            id.isBlank() || password.isBlank() ->
                Result.failure(IllegalArgumentException("아이디와 비밀번호를 입력하세요."))
            id == DEMO_ID && password == DEMO_PW -> Result.success(Unit)
            else -> Result.failure(IllegalStateException("아이디 또는 비밀번호가 올바르지 않습니다."))
        }
    }

    private companion object {
        const val DEMO_ID = "deltax"
        const val DEMO_PW = "deltax"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    abstract fun bindAuthRepository(impl: StubAuthRepository): AuthRepository
}
