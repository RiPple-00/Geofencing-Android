#### Setup

1. `local.properties.example`을 복사해서 `local.properties`로 이름 변경
2. Google Maps API 키를 발급받아 `MAPS_API_KEY` 값 채우기
3. Gradle sync

#### 기술 스택

- Kotlin, Jetpack Compose, Material 3
- Hilt (DI), Navigation Compose
- Google Maps Compose
- Haze (블러 효과)

#### 요구 사항

- Android Studio (최신 안정 버전 권장)
- minSdk 26 / targetSdk 37 / compileSdk 37

#### 프로젝트 구조

```
app/src/main/java/com/example/geofencing
├── di            # Hilt 모듈
├── data
│   ├── model       # Geofence, Sector 등 데이터 모델
│   └── repository  # 데이터 소스 접근
└── ui
    ├── home        # 홈 화면
    ├── map         # 지도 화면 (검색, 마커, 사이드 패널)
    ├── detail      # 지오펜스 상세 화면
    ├── components  # 공용 컴포넌트
    ├── navigation  # 네비게이션 그래프
    └── theme       # 컬러/타이포그래피/스페이싱 등 공용 테마
```