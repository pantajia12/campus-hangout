# 개발 규칙

## Git

### 브랜치
- `main`: 항상 빌드가 되는 상태로 유지합니다. 직접 push하지 않고 PR로만 머지합니다. (Phase 0 초기 세팅은 예외)
- 작업 브랜치 이름은 `타입/#이슈번호-짧은-설명` 형식입니다.
  - 예: `feat/#12-signup-api`, `fix/#30-token-refresh`, `docs/#5-erd`

### 커밋 메시지 (Conventional Commits)
```
타입(범위): 요약

본문 (무엇을, 왜)
```
| 타입 | 용도 |
|---|---|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변화 없는 구조 개선 |
| `test` | 테스트 추가·수정 |
| `docs` | 문서 |
| `chore` | 빌드 설정, 의존성 등 |
| `ci` | CI 설정 |

범위는 `server`, `android`, `infra` 중 하나를 씁니다. 예: `feat(server): 학생증 심사 승인 API`

### PR
- push 전에 [로컬 테스트 가이드](guides/local-testing.md)의 해당 Phase 체크리스트를 직접 확인합니다.
- 이슈 하나에 PR 하나를 원칙으로 합니다.
- CI가 통과해야 머지할 수 있습니다.
- 머지는 **Squash and merge**로 합니다.
- PR 템플릿의 체크리스트를 채웁니다.

### 태그와 릴리즈
- Phase가 끝나면 `v0.N.0` 태그를 달고 GitHub Release에 변경 내역을 적습니다.
- 버그 수정만 있는 배포는 `v0.N.1`처럼 패치 번호를 올립니다.

### 비밀값
- `.env`, keystore, API 키, 비밀번호는 절대 커밋하지 않습니다.
- 서버는 환경 변수로, 앱은 `local.properties`로, CI는 GitHub Secrets로 넣습니다.

## API 규칙

- 모든 경로는 `/api/v1`로 시작합니다.
- JSON 필드는 camelCase로 씁니다.
- 시간은 ISO-8601 UTC 문자열입니다. 예: `2026-11-02T09:30:00Z`
- 모든 응답은 같은 봉투(envelope) 형식을 씁니다.

성공
```json
{ "success": true, "data": { "status": "ok" } }
```

실패
```json
{
  "success": false,
  "error": {
    "code": "INVALID_INPUT",
    "message": "입력값이 올바르지 않습니다.",
    "details": [{ "field": "studentNumber", "reason": "8자리 숫자여야 합니다." }]
  }
}
```

- 앱은 `error.code`로 분기하고, 사용자에게 보여 줄 문구는 앱의 다국어 리소스에서 가져옵니다. `message`는 개발자용입니다.
- 목록 API는 커서 페이지네이션을 씁니다. `?cursor=...&size=20` 요청에 `{ "items": [...], "nextCursor": "..." }`로 응답합니다.

## 코드 스타일

- Kotlin 공식 코드 스타일을 따릅니다. (Android Studio: Settings → Editor → Code Style → Kotlin → Set from… → Kotlin style guide)
- 서버 패키지는 기능(도메인)별로 나눕니다. 예: `auth`, `verification`, `profile`, `room`, `chat`
- 앱 패키지는 `designsystem`, `navigation`, `feature/<기능>`, 그리고 Phase 1부터 `core/<data|network>`로 나눕니다.
- 사용자에게 보이는 문자열은 코드에 직접 쓰지 않고 `strings.xml`에 둡니다. 기본은 영어(`values/`), 한국어는 `values-ko/`입니다.
