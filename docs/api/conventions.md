# API Conventions

Rules shared by every endpoint. Area-specific specs: [`social.md`](social.md) (rooms, chat, knocks, reports), `account.md` and `profile.md` (account, profile).

## Base URL

| Where the app runs | Base URL |
|---|---|
| Android emulator, server on the same PC | `http://10.0.2.2:8080` |
| Real phone, server on a PC in the same Wi-Fi | `http://<PC's IPv4 address>:8080` |
| Deployed server | announced when deployed |

All new endpoints live under **`/api/v1`**. The first account endpoints still use `/api/...` and are being moved to `/api/v1/...`.

## Response envelope

Success:

```json
{ "success": true, "data": { "id": 1 } }
```

Failure:

```json
{
  "success": false,
  "error": {
    "code": "ROOM_FULL",
    "message": "Developer-facing explanation, may be Korean",
    "details": [ { "field": "title", "reason": "must not be blank" } ]
  }
}
```

- `data` is omitted when there is nothing to return. `details` appears only for validation errors.
- **The app decides what to show from `error.code`**, using its own localized strings. `message` is for debugging only.

## HTTP status codes

| Status | Meaning | Typical `error.code` |
|---|---|---|
| 200 | OK | |
| 201 | Created | |
| 202 | Accepted, waiting for someone else (e.g. host approval) | |
| 400 | Invalid input | `INVALID_INPUT` (+ `details`) or a specific code |
| 401 | Not logged in, or token expired/invalid → go to the login screen | `UNAUTHORIZED` |
| 403 | Logged in but not allowed (not approved yet, not the host, ...) | `FORBIDDEN` or a specific code |
| 404 | Not found | `NOT_FOUND` or a specific code |
| 409 | Conflicts with the current state (full, duplicate, ...) | specific code |
| 413 | Uploaded file too large | `INVALID_INPUT` |
| 500 | Server bug | `INTERNAL_ERROR` |

Error codes are `UPPER_SNAKE_CASE` with an area prefix: account `AUTH_`, `VERIFY_`, `PROFILE_`, `ADMIN_`; social `ROOM_`, `CHAT_`, `KNOCK_`, `FRIEND_`, `REPORT_`. Common codes have no prefix.

## Authentication

- Log in → receive `accessToken` (JWT, valid 24 hours).
- Send it on every request: `Authorization: Bearer <accessToken>`.
- There is no refresh token yet: on `401`, clear the token and show the login screen.
- Until the student ID is approved, only sign-up/profile/verification endpoints work; everything else returns `403`.

## Data formats

| Kind | Format | Example |
|---|---|---|
| IDs | number (64-bit) | `42` |
| Date-time | ISO-8601 string in **UTC** | `"2026-12-03T09:30:00Z"` |
| Enums | upper-case string | `"REAL_NAME"` |
| Country | ISO 3166-1 alpha-2 | `"KR"`, `"NO"` |
| Text | UTF-8, trimmed by the server | |

- Show times in the phone's time zone (Korea: KST, UTC+9). Send times in UTC.
- A field that can be empty is sent as `null`, not omitted (except `data` / `details` in the envelope).

## Paging

Lists that can grow use one of two styles:

**Page style** (browsing lists, e.g. Explore):

```
GET /api/v1/rooms?page=0&size=20
→ { "items": [...], "page": 0, "size": 20, "hasNext": true }
```

**Before-ID style** (chat history, newest first):

```
GET /api/v1/rooms/7/messages?size=50                 ← newest 50
GET /api/v1/rooms/7/messages?before=1234&size=50     ← 50 messages older than id 1234
→ { "items": [...], "hasNext": true }
```

`size` defaults to 20 (50 for messages) and is capped at 100.

## Real-time events

One WebSocket per logged-in app: `ws://<host>/ws?token=<accessToken>` (`wss://` when deployed). The server pushes JSON events; the client never sends anything except pings. Details in [`social.md` §5](social.md#5-real-time-events-websocket).
