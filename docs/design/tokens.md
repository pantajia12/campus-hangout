# 디자인 토큰

Figma와 앱 코드가 같은 값을 쓰도록 여기서 관리합니다.
코드 위치: `android/app/src/main/java/com/caubuddy/android/designsystem/theme/`

## 색 (Color.kt)

| 토큰 | 값 | 쓰임 |
|---|---|---|
| Blue500 | `#2F5BEA` | primary (라이트) — 주요 버튼, 선택된 탭 |
| Blue200 | `#B4C5FF` | primary (다크) |
| Blue50 | `#EEF2FF` | primaryContainer — 선택된 칩, 탭 인디케이터 |
| Blue900 | `#0B1F66` | onPrimaryContainer |
| Coral500 | `#FF7A59` | secondary — 친구·노크 같은 따뜻한 강조 |
| Coral200 | `#FFB59F` | secondary (다크) |
| Coral50 | `#FFF0EB` | secondaryContainer |
| Gray0 | `#FFFFFF` | surface (라이트) |
| Gray50 | `#F6F7F9` | background (라이트) |
| Gray100 | `#EDEFF3` | surfaceVariant |
| Gray300 | `#C9CED6` | outline |
| Gray500 | `#8A919C` | 비활성 텍스트 |
| Gray700 | `#4A5059` | onSurfaceVariant (보조 텍스트) |
| Gray900 | `#1B1E23` | onSurface (본문), surface (다크) |
| Gray950 | `#111316` | background (다크) |
| Red500 / Red200 | `#D93F3F` / `#FFB4AB` | error (라이트 / 다크) |

규칙
- 화면 코드에서는 토큰을 직접 쓰지 않고 `MaterialTheme.colorScheme.primary`처럼 **역할**로 접근합니다. 그래야 다크모드가 자동으로 맞춰집니다.
- 브랜드 색을 지키기 위해 Android 12+ 동적 색상은 쓰지 않습니다.

## 간격 (Spacing.kt)

| 토큰 | 값 |
|---|---|
| xxs | 2dp |
| xs | 4dp |
| sm | 8dp |
| md | 12dp |
| lg | 16dp (화면 좌우 기본 여백) |
| xl | 24dp |
| xxl | 32dp |

## 모서리 (Shape.kt)

| 토큰 | 값 | 예시 |
|---|---|---|
| extraSmall | 4dp | 배지 |
| small | 8dp | 칩, 작은 버튼 |
| medium | 12dp | 카드, 입력창 |
| large | 16dp | 바텀시트, 말풍선 |
| extraLarge | 24dp | 다이얼로그 |

## 글꼴 (Type.kt)

- 지금은 Material 3 기본 크기를 쓰고, 제목(titleLarge, titleMedium) 굵기만 키웠습니다.
- Phase 7에서 한글 가독성이 좋은 글꼴(예: Pretendard)을 적용할 예정입니다.

## 공통 컴포넌트

| 컴포넌트 | 설명 |
|---|---|
| `CbTopAppBar` | 화면 상단 바 |
| `EmptyState` | 데이터가 없을 때 아이콘 + 제목 + 설명 |

새 공통 컴포넌트는 `designsystem/component/`에 `Cb` 접두어를 붙여 추가합니다.
