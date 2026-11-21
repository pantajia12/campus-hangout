# Account API 구현 문서

Campus Hangout 백엔드의 회원가입, 로그인 및 학생증 인증 기능에 대한 구현 문서입니다.

## 1. 개요

### 구현 기능

- 이메일 및 비밀번호 기반 회원가입
- JWT 기반 로그인 및 사용자 인증
- 학생증 이미지 업로드 및 검증
- 학생증 인증 상태 조회
- 중복 제출 방지 및 예외 처리

### API 목록

| Method | Endpoint | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/auth/signup` | 회원가입 | 불필요 |
| POST | `/api/auth/login` | 로그인 | 불필요 |
| POST | `/api/student-verifications` | 학생증 인증 신청 | 필요 |
| GET | `/api/student-verifications/me` | 내 학생증 인증 상태 조회 | 필요 |

**기본 URL:** `http://localhost:8080`

인증이 필요한 API는 다음 헤더를 사용합니다.

```http
Authorization: Bearer <JWT_ACCESS_TOKEN>
```

JWT 유효기간은 24시간입니다.

---

## 2. API 상세

### 2.1 회원가입

`POST /api/auth/signup`

이메일과 비밀번호를 입력받아 새로운 사용자 계정을 생성합니다.

**요청**

```json
{
  "email": "student@cau.ac.kr",
  "password": "password123"
}
```

**성공 응답 — 201 Created**

```json
{
  "success": true,
  "data": {
    "userId": 1
  }
}
```

**처리 과정**

1. 이메일 및 비밀번호 유효성 검사
2. 이메일 중복 여부 확인
3. 비밀번호 암호화
4. 사용자 정보 저장
5. 생성된 사용자 ID 반환

**유효성 검사**

| 필드 | 조건 |
|---|---|
| email | 필수, 공백 불가, 이메일 형식 |
| password | 필수, 공백 불가, 최소 8자 |

**주요 오류**

| HTTP 상태 | 발생 조건 |
|---|---|
| 400 | 이메일 형식 오류, 필수값 누락, 비밀번호 길이 미달 |
| 409 | 이미 등록된 이메일 |

회원가입 시 `verified` 값은 `false`로 초기화됩니다.

### 2.2 로그인

`POST /api/auth/login`

등록된 이메일과 비밀번호를 검증하고 JWT 액세스 토큰을 발급합니다.

**요청**

```json
{
  "email": "student@cau.ac.kr",
  "password": "password123"
}
```

**성공 응답 — 200 OK**

```json
{
  "success": true,
  "data": {
    "accessToken": "<JWT_ACCESS_TOKEN>"
  }
}
```

**처리 과정**

1. 이메일로 사용자 조회
2. 입력된 비밀번호와 저장된 암호화 비밀번호 비교
3. 인증 성공 시 사용자 ID 기반 JWT 생성
4. 액세스 토큰 반환

**주요 오류**

| HTTP 상태 | 발생 조건 |
|---|---|
| 400 | 요청 데이터 유효성 검사 실패 |
| 401 | 이메일 또는 비밀번호 불일치 |

보안을 위해 이메일이 존재하지 않는 경우와 비밀번호가 틀린 경우 동일한 예외를 사용합니다.

### 2.3 학생증 인증 신청

`POST /api/student-verifications`

학생증 이미지를 제출하여 사용자 인증을 신청합니다.

**요청 형식:** `multipart/form-data`

| 필드 | 타입 | 필수 | 조건 |
|---|---|---|---|
| file | File | O | JPG 또는 PNG, 최대 5MB |

**요청 예시**

```bash
curl -X POST http://localhost:8080/api/student-verifications \
  -H "Authorization: Bearer <JWT_ACCESS_TOKEN>" \
  -F "file=@student-id.png"
```

**성공 응답 — 201 Created**

자동 승인 활성화 시:

```json
{
  "success": true,
  "data": {
    "status": "APPROVED"
  }
}
```

자동 승인 비활성화 시 `status`는 `PENDING`입니다.

위 예시는 주요 필드만 표시한 것으로, JSON 직렬화 설정에 따라 `rejectionReason: null`이 포함될 수 있습니다.

**처리 과정**

1. JWT로 인증된 사용자 ID 확인
2. 사용자 존재 여부 확인
3. 기존 학생증 제출 이력 확인
4. 파일 크기 및 이미지 형식 검증
5. 이미지 파일 저장
6. 학생증 인증 정보 저장
7. 자동 승인 설정에 따라 인증 상태 결정
8. 인증 결과 반환

**파일 검증**

- 허용 형식: JPEG, PNG
- 최대 크기: 5MB
- 파일의 MIME 타입과 실제 파일 시그니처 검사
- 유효하지 않은 파일은 저장하지 않음
- 저장 파일명은 UUID 기반으로 생성

**주요 오류**

| HTTP 상태 | 발생 조건 | 오류 코드 |
|---|---|---|
| 400 | 빈 파일, 지원하지 않는 이미지 형식 | `INVALID_INPUT` |
| 400 | 필수 파일 누락 | `INVALID_INPUT` |
| 401 | JWT 누락 또는 인증 실패 | `UNAUTHORIZED` |
| 404 | 사용자 조회 실패 | `NOT_FOUND` |
| 409 | 학생증 중복 제출 | `CONFLICT` |
| 413 | 파일 크기 5MB 초과 | `INVALID_INPUT` |

**413 응답 예시**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_INPUT",
    "message": "파일 크기는 5MB를 초과할 수 없습니다."
  }
}
```

**구현 시 주의사항**

- 사용자당 학생증 제출은 1회만 허용합니다.
- 승인 또는 반려 이후에도 재제출은 허용하지 않습니다.
- 학생증 이미지 경로는 API 응답에 포함하지 않습니다.
- 현재 관리자 수동 승인 API는 구현하지 않았습니다.

### 2.4 학생증 인증 상태 조회

`GET /api/student-verifications/me`

현재 로그인한 사용자의 학생증 인증 상태를 조회합니다.

**요청 본문:** 없음

**성공 응답 — 200 OK**

```json
{
  "success": true,
  "data": {
    "status": "APPROVED"
  }
}
```

`rejectionReason`은 nullable 필드이며, 반려 시 반려 사유를 반환할 수 있습니다.

**인증 상태**

| 상태 | 설명 |
|---|---|
| `NOT_SUBMITTED` | 학생증 미제출 |
| `PENDING` | 인증 심사 대기 |
| `APPROVED` | 인증 승인 |
| `REJECTED` | 인증 반려 |

**처리 과정**

1. JWT에서 인증된 사용자 ID 확인
2. 사용자의 최신 학생증 인증 정보 조회
3. 제출 이력이 없으면 `NOT_SUBMITTED` 반환
4. 제출 이력이 있으면 해당 인증 상태와 반려 사유 반환

**주요 오류**

| HTTP 상태 | 발생 조건 |
|---|---|
| 401 | JWT 누락, 만료 또는 인증 실패 |

---

## 3. 공통 인증 및 예외 처리

### 3.1 JWT 인증

Spring Security 기반의 JWT 인증을 사용합니다.

- 회원가입 및 로그인 API는 인증 없이 접근 가능합니다.
- 그 외 API는 기본적으로 인증이 필요합니다.
- JWT는 사용자 ID를 기반으로 생성합니다.
- JWT 필터에서 토큰 유효성과 사용자 존재 여부를 확인합니다.
- 서버는 세션을 사용하지 않는 Stateless 방식으로 동작합니다.

### 3.2 공통 응답 형식

**성공**

```json
{
  "success": true,
  "data": {}
}
```

**실패**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_INPUT",
    "message": "입력값이 올바르지 않습니다."
  }
}
```

유효성 검사 실패 시 `details` 필드에 오류가 발생한 필드와 사유가 포함될 수 있습니다.

### 3.3 공통 오류 코드

| 오류 코드 | HTTP 상태 | 설명 |
|---|---|---|
| `INVALID_INPUT` | 400 | 잘못된 요청 |
| `UNAUTHORIZED` | 401 | 인증 필요 또는 인증 실패 |
| `FORBIDDEN` | 403 | 접근 권한 없음 |
| `NOT_FOUND` | 404 | 리소스 없음 |
| `METHOD_NOT_ALLOWED` | 405 | 지원하지 않는 HTTP 메서드 |
| `CONFLICT` | 409 | 요청 상태 충돌 |
| `INTERNAL_ERROR` | 500 | 서버 내부 오류 |

파일 크기 초과 예외는 별도로 처리하여 HTTP 413을 반환합니다.

---

## 4. 학생증 인증 환경 설정

학생증 인증은 환경에 따라 자동 승인 여부가 달라집니다.

| 환경 | 자동 승인 | 제출 시 상태 |
|---|---|---|
| local | 활성화 | `APPROVED` |
| test | 활성화 | `APPROVED` |
| prod | 기본 비활성화 | `PENDING` |

자동 승인 활성화 시 학생증 인증 정보가 `APPROVED`로 저장되고 사용자의 `verified` 값도 `true`로 변경됩니다.

자동 승인 비활성화 시 인증 정보는 `PENDING` 상태로 저장됩니다.

현재 관리자 승인 기능이 없으므로 운영 환경에서의 승인 절차는 추후 구현이 필요합니다.

---

## 5. 테스트

Spring Boot Test, MockMvc, JUnit 5를 사용하여 API 동작을 검증합니다.

### 주요 테스트 항목

| 구분 | 검증 내용 |
|---|---|
| 회원가입 | 사용자 생성 및 입력값 검증 |
| 로그인 | 사용자 인증 및 JWT 발급 |
| 인증 | JWT 누락 시 401 반환 |
| 학생증 제출 | 정상 제출 시 201 반환 |
| 인증 상태 조회 | 미제출 시 `NOT_SUBMITTED` 반환 |
| 중복 제출 | 두 번째 제출 시 409 반환 |
| 이미지 검증 | 지원하지 않는 형식 및 위조된 PNG 거부 |
| 파일 크기 | 5MB 초과 시 413 반환 |
| 개인정보 보호 | 응답에 이미지 경로가 포함되지 않는지 확인 |

학생증 업로드 크기 테스트는 MockMvc를 통해 파일 저장소의 크기 검증 및 예외 처리 경로를 확인합니다. 실제 HTTP 서버의 multipart 제한 동작은 별도 검증 대상입니다.

**전체 테스트 실행**

```bash
cd server
./gradlew test
```

---

## 6. 미구현 기능 및 향후 작업

현재 Account 기능 중 다음 항목은 추후 구현 예정입니다.

- 사용자 프로필 조회 및 수정 (`UserProfile`)
- 사용자 언어 관리 (`UserLanguage`, `Language`)
- 사용자 관심사 관리 (`UserInterest`, `Interest`)
- 관리자 학생증 승인 및 반려
- 인증 완료 사용자에 대한 모임·채팅 접근 제한

이메일 인증 기능은 현재 구현 범위에 포함하지 않습니다.

---

## 7. 프론트엔드 연동 문서

프론트엔드 연동을 위한 상세 요청·응답 예시 및 오류별 JSON은 별도로 관리하는 영어 Notion API Specification을 참고합니다.

API의 요청 형식이나 응답 구조가 변경되는 경우 해당 명세서도 함께 업데이트해야 합니다.
