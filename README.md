# Campus Hangout

An Android app for Chung-Ang University students to meet international friends and study partners.
Students create or join **meetup rooms** by category (date, place, capacity), chat in real time, and send "knocks" to make friends.
Only students verified by their student ID card can sign up.

**Team docs (English):** [Team Guide](docs/team/TEAM_GUIDE.md) · [API conventions](docs/api/conventions.md) · [Social API](docs/api/social.md)

## Team

| Member | GitHub | Role |
|---|---|---|
| Yechan Han | [@pantajia12](https://github.com/pantajia12) | Backend (meetup rooms, chat, knocks, reports), Android networking |
| Hyeonsu | [@usernamehs](https://github.com/usernamehs) | Backend (sign-up, login, student ID verification, profile) |
| Håkon | — | Design (Figma), Android UI |

## Tech stack

Android: Kotlin, Jetpack Compose, Material 3 · Server: Kotlin, Spring Boot 4, JDK 21 · DB: PostgreSQL 17, Redis 7 · CI: GitHub Actions

---

## 한국어

중앙대학교 학생이 **관심사가 같은 외국인 친구**, **같이 공부할 친구**를 찾는 앱입니다.
카카오톡 오픈채팅처럼 카테고리별 **모임 방**(날짜·장소·정원)을 만들고 참여해 대화하고, 학생증 인증을 거친 학생만 가입할 수 있습니다.

## 주요 기능 (계획)

- 학생증 제출 → 관리자 승인 가입
- 프로필 꾸미기, 관심 분야·사용 언어 설정
- 카테고리별 모임 방 만들기/참여 (**실명방 / 익명방**, 바로 참여 / 방장 승인)
- 실시간 그룹 채팅
- 관심사·언어 기반 친구 추천, 인사 메시지와 함께 보내는 "노크"
- 한국어 / 영어 지원

## 진행 상황

| Phase | 내용 | 상태 |
|---|---|---|
| 0 | 프로젝트 뼈대 | ✅ `v0.0.5` |
| 1 | 가입 4단계(학생증 제출) · 로그인 · 관리자 심사 | ⏳ 다음 |
| 2 | 프로필 꾸미기 · 관심사 | |
| 3 | 카테고리 모임 방 (익명/실명, 참여 요청) | |
| 4 | 실시간 채팅 (MVP) | |
| 5 | 친구 추천 · 노크 · 1:1 대화 | |
| 6 | 안전 · 알림 · 다국어 | |
| 7 | 운영 배포 · 출시 | |

자세한 계획: [docs/ROADMAP.md](docs/ROADMAP.md)

## 기술 스택

| 영역 | 사용 기술 |
|---|---|
| 앱 | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| 서버 | Kotlin, Spring Boot 4, JDK 21 |
| DB | PostgreSQL 17, Redis 7 |
| 인프라 | Docker Compose, Azure for Students (VM · PostgreSQL · Blob Storage), GitHub Actions |

## 저장소 구조

```
campus-hangout/
├── android/   # Android 앱 → android/README.md
├── server/    # API + 채팅 서버 → server/README.md
├── infra/     # Docker Compose 등 → infra/README.md
├── docs/      # 로드맵, 설계, 가이드
├── tools/     # 개발 보조 스크립트 (Figma 내보내기 등)
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

- [팀 가이드 (영어, 2주 일정·역할·규칙)](docs/team/TEAM_GUIDE.md)
- [API 공통 규칙 (영어)](docs/api/conventions.md) · [모임·채팅·노크·신고 API (영어)](docs/api/social.md)
- [로드맵](docs/ROADMAP.md)
- [개발 규칙 (Git, API, 코드 스타일)](docs/conventions.md)
- [아키텍처 · ERD · 익명/실명 설계](docs/architecture.md)
- [디자인 토큰](docs/design/tokens.md)
- [Figma 디자인 분석 (화면 목록 · 기존 결정과 다른 점)](docs/design/figma-review.md)
- [Figma 데이터 내보내기 도구](tools/figma/README.md)
- [Azure for Students 가입과 무료 자원](docs/guides/azure-for-students-setup.md)
- [로컬 테스트 가이드 (push 전에 확인)](docs/guides/local-testing.md)

## 만든 사람

위 [Team](#team) 표를 보세요.
