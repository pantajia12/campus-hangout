# 개발 규칙

> **2026-11-26부터 팀 공용 규칙은 [Team Guide (영어)](team/TEAM_GUIDE.md)가 기준입니다.** 팀원 Håkon이 영어만 쓰므로 커밋 메시지·PR·이슈는 영어로 씁니다. 이 문서는 참고용으로 남겨 둡니다.

## Git

### 브랜치
- `main`: 항상 빌드가 되는 상태로 유지합니다. 직접 push하지 않고 PR로만 머지합니다. (Phase 0 초기 세팅은 예외)
- 작업 브랜치 이름은 `타입/#이슈번호-짧은-설명` 형식입니다.
  - 예: `feat/#12-signup-api`, `fix/#30-token-refresh`, `docs/#5-erd`

### 커밋 메시지 (Conventional Commits)
```
타입(범위): 요약                     ← 제목 (50자 이내)
                                     ← 빈 줄
본문: 무엇을, 왜 바꿨는지             ← 한 줄 72자 안팎
                                     ← 빈 줄
Closes #12                           ← 꼬리말: 관련 이슈 (있을 때만)
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

범위는 `server`, `android`, `infra` 중 하나를 씁니다. 여러 곳에 걸치거나 문서만 바꾸면 생략합니다.

**규칙**
1. 제목은 `~ 추가`, `~ 수정`, `~ 삭제`처럼 무엇을 했는지로 끝내고, 마침표를 찍지 않습니다.
2. 커밋 하나에는 목적 하나만 담습니다. 기능 추가와 버그 수정을 한 커밋에 섞지 않습니다.
3. 본문에는 코드만 봐서는 알 수 없는 **이유**를 적습니다. (무엇이 문제였고, 왜 이렇게 고쳤는지)
4. 빌드가 깨진 상태로 커밋하지 않습니다.

**좋은 예 / 나쁜 예**
| 좋은 예 | 나쁜 예 | 이유 |
|---|---|---|
| `feat(server): 학생증 심사 승인 API 추가` | `api 작업` | 무엇을 했는지 알 수 없음 |
| `fix(android): 탭 이동 시 카테고리 선택이 초기화되는 문제 수정` | `버그 수정` | 어떤 버그인지 알 수 없음 |
| `refactor(server): 예외 응답 생성 코드를 한 곳으로 정리` | `코드 정리 및 로그인 기능 추가` | 목적이 두 개 섞임 |

**커밋 템플릿 사용 (권장)**
저장소 맨 위의 `.gitmessage`를 템플릿으로 지정하면 `git commit`(메시지 없이)을 실행할 때 위 규칙이 적힌 편집 창이 열립니다.
```powershell
git config commit.template "C:/dev/campus-hangout/.gitmessage"
git config core.editor notepad      # 편집 창을 메모장으로 (기본은 vim)
```
메모장에 메시지를 쓰고 저장한 뒤 닫으면 커밋됩니다. `#`으로 시작하는 줄은 메시지에 들어가지 않습니다.

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
