# CAU Buddy (가칭)

중앙대학교 학생이 **관심사가 같은 외국인 친구**, **같이 공부할 친구**를 찾는 앱입니다.
카카오톡 오픈채팅처럼 카테고리별 채팅방을 만들고 참여할 수 있고, 학생증 인증을 거친 학생만 가입할 수 있습니다.

## 주요 기능 (계획)

- 학생증 제출 → 관리자 승인 가입
- 프로필 꾸미기, 관심 분야·사용 언어 설정
- 카테고리별 오픈채팅방 만들기/참여 (**실명방 / 익명방**)
- 실시간 채팅
- 관심사·언어 기반 친구 추천, 1:1 "노크"
- 한국어 / 영어 지원

## 진행 상황

| Phase | 내용 | 상태 |
|---|---|---|
| 0 | 프로젝트 뼈대 | ✅ `v0.0.1` |
| 1 | 학생증 인증 가입 · 로그인 · 관리자 승인 | ⏳ 다음 |
| 2 | 프로필 꾸미기 · 관심사 | |
| 3 | 카테고리 오픈채팅방 (익명/실명) | |
| 4 | 실시간 채팅 (MVP) | |
| 5 | 친구 추천 · 노크 | |
| 6 | 안전 · 알림 · 다국어 | |
| 7 | 운영 배포 · 출시 | |

자세한 계획: [docs/ROADMAP.md](docs/ROADMAP.md)

## 기술 스택

| 영역 | 사용 기술 |
|---|---|
| 앱 | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| 서버 | Kotlin, Spring Boot 4, JDK 21 |
| DB | PostgreSQL 17, Redis 7 |
| 인프라 | Docker Compose, Oracle Cloud Always Free, GitHub Actions |

## 저장소 구조

```
cau-buddy/
├── android/   # Android 앱 → android/README.md
├── server/    # API + 채팅 서버 → server/README.md
├── infra/     # Docker Compose 등 → infra/README.md
├── docs/      # 로드맵, 설계, 가이드
└── .github/   # CI, PR/이슈 템플릿
```

## 빠르게 실행해 보기

```bash
# 1) 로컬 DB (Phase 1부터 필요)
cd infra && cp .env.example .env && docker compose up -d

# 2) 서버 (JDK 21 필요)
cd server && ./gradlew bootRun
curl http://localhost:8080/api/v1/ping

# 3) 앱: Android Studio에서 android/ 폴더를 열고 Run
```

## 문서

- [로드맵](docs/ROADMAP.md)
- [개발 규칙 (Git, API, 코드 스타일)](docs/conventions.md)
- [아키텍처 · ERD · 익명/실명 설계](docs/architecture.md)
- [디자인 토큰](docs/design/tokens.md)
- [Oracle Cloud 무료 계정 가입](docs/guides/oracle-cloud-setup.md)
