# android

Kotlin + Jetpack Compose 앱입니다.

## 실행

1. Android Studio에서 **`android/` 폴더**를 엽니다. (저장소 최상위 폴더가 아닙니다)
2. Gradle Sync가 끝날 때까지 기다립니다. 처음에는 의존성을 내려받느라 몇 분 걸립니다.
3. 에뮬레이터(API 26 이상)나 실기기를 고르고 ▶ Run을 누릅니다.

명령줄로 빌드할 때는 아래처럼 합니다.

```bash
cd android
./gradlew assembleDebug    # 디버그 APK 빌드
./gradlew lintDebug        # 린트
```

## 기준 버전

| 항목 | 버전 |
|---|---|
| minSdk / targetSdk | 26 / 36 |
| Kotlin | 2.3.21 |
| Android Gradle Plugin | 8.13.0 |
| Compose BOM | 2025.10.00 |

Android Studio가 AGP 업그레이드를 제안하더라도 Phase 진행 중에는 바로 올리지 않습니다. 버전은 Phase가 끝날 때 한 번에 올립니다.
