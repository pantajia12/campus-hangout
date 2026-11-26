# Social API — rooms, chat, knocks, friends, reports

Owner: Yechan (@pantajia12). Read [`conventions.md`](conventions.md) first (envelope, errors, auth, time, paging).

Status of each part: **spec** = agreed shape, not implemented yet · **done** = implemented on `main`.

| Part | Status |
|---|---|
| 1. Categories | spec |
| 2. Rooms | spec |
| 3. Membership & join requests | spec |
| 4. Chat messages, My chats | spec |
| 5. Real-time events (WebSocket) | spec |
| 6. Knocks & friends | spec |
| 7. Reports | spec |

All endpoints require `Authorization: Bearer <token>` of an **approved** user.

---

## Shared objects

```kotlin
@Serializable
data class Category(
    val id: Long,
    val code: String,      // "FOOD_CAFE"
    val name: String,      // English label, e.g. "Food & Cafe"
    val nameKo: String,    // Korean label, e.g. "맛집·카페"
)

@Serializable
data class UserSummary(
    val userId: Long,
    val displayName: String,   // verified real name
    val nationality: String?,  // ISO 3166-1 alpha-2, e.g. "KR"
    val major: String?,
    val avatarColor: Int,      // 1..4, mapped to a theme color in the app
)

@Serializable
data class RoomSummary(
    val id: Long,
    val title: String,
    val category: Category,
    val startsAt: String,      // ISO-8601 UTC
    val location: String,
    val maxMembers: Int,
    val memberCount: Int,
    val mode: String,          // "REAL_NAME" | "ANONYMOUS"
    val joinPolicy: String,    // "OPEN" | "APPROVAL"
    val status: String,        // "OPEN" | "FULL" | "ENDED" | "CANCELED"
    val myStatus: String,      // "NONE" | "PENDING" | "MEMBER" | "HOST"
)
```

`status`:
- `OPEN` – people can join
- `FULL` – `memberCount == maxMembers`
- `ENDED` – more than 12 hours after `startsAt`; the chat still works for members, nobody can join
- `CANCELED` – the host canceled the meetup; the chat is read-only

`myStatus` maps to the five Figma "Room details" states:

| `myStatus` | `status` | `joinPolicy` | Button in Figma |
|---|---|---|---|
| `NONE` | `OPEN` | `OPEN` | **I'm in!** → `POST /join` |
| `NONE` | `OPEN` | `APPROVAL` | **Request to join** → `POST /join` |
| `PENDING` | any | `APPROVAL` | **Request sent** (tap to cancel) → `DELETE /join-request` |
| `NONE` | `FULL` | any | **Room is full** (disabled) |
| `MEMBER` / `HOST` | any | any | **Open chat** |

---

## 1. Categories

### `GET /api/v1/room-categories`

The six Figma categories, in display order. Rarely changes — cache it for the app session.

```json
{ "success": true, "data": [
  { "id": 1, "code": "FOOD_CAFE",      "name": "Food & Cafe",       "nameKo": "맛집·카페" },
  { "id": 2, "code": "STUDY_LANGUAGE", "name": "Study & Language",  "nameKo": "스터디·언어" },
  { "id": 3, "code": "HOBBY_CULTURE",  "name": "Hobbies & Culture", "nameKo": "취미·문화" },
  { "id": 4, "code": "SPORTS_OUTDOOR", "name": "Sports & Outdoors", "nameKo": "운동·야외" },
  { "id": 5, "code": "TRAVEL_EXPLORE", "name": "Travel & Explore",  "nameKo": "여행·탐방" },
  { "id": 6, "code": "CAMPUS_LIFE",    "name": "Campus Life",       "nameKo": "캠퍼스 생활" }
] }
```

---

## 2. Rooms

### `GET /api/v1/rooms` — Explore

Upcoming rooms (not `ENDED`, not `CANCELED`), soonest first.

| Query | Default | Meaning |
|---|---|---|
| `categoryId` | all | filter by category |
| `q` | — | search in title and description (case-insensitive) |
| `page`, `size` | 0, 20 | page style paging |

```json
{ "success": true, "data": {
  "items": [ {
    "id": 7, "title": "Korean BBQ near campus",
    "category": { "id": 1, "code": "FOOD_CAFE", "name": "Food & Cafe", "nameKo": "맛집·카페" },
    "startsAt": "2026-12-03T09:30:00Z", "location": "Heukseok station exit 4",
    "maxMembers": 6, "memberCount": 3,
    "mode": "REAL_NAME", "joinPolicy": "OPEN",
    "status": "OPEN", "myStatus": "NONE"
  } ],
  "page": 0, "size": 20, "hasNext": false
} }
```

### `POST /api/v1/rooms` — Create room → `201`

The creator becomes the host and the first member.

```json
{
  "title": "Korean BBQ near campus",
  "description": "Let's try samgyeopsal! Everyone welcome.",
  "categoryId": 1,
  "startsAt": "2026-12-03T09:30:00Z",
  "location": "Heukseok station exit 4",
  "maxMembers": 6,
  "mode": "REAL_NAME",
  "joinPolicy": "OPEN",
  "hostNickname": null
}
```

| Field | Rule |
|---|---|
| `title` | 1–40 characters |
| `description` | 0–1000 characters |
| `categoryId` | an existing category |
| `startsAt` | in the future |
| `location` | 1–100 characters |
| `maxMembers` | 2–30 |
| `mode` | `REAL_NAME` or `ANONYMOUS` — cannot be changed later |
| `joinPolicy` | `OPEN` or `APPROVAL` |
| `hostNickname` | required (2–20 characters) when `mode` is `ANONYMOUS`, otherwise ignored |

Response `data`: `RoomDetail` (below).

### `GET /api/v1/rooms/{roomId}` — Room details

```kotlin
@Serializable
data class RoomDetail(
    // every RoomSummary field, plus:
    val description: String,
    val host: MemberView,           // see §3
    val myNickname: String?,        // my nickname in an anonymous room
    val pendingRequestCount: Int?,  // only for the host, otherwise null
    val createdAt: String,
)
```

Anyone approved can read details of any room (to decide whether to join). Canceled rooms return `ROOM_NOT_FOUND` to non-members.

### `PATCH /api/v1/rooms/{roomId}` — Edit (host only) → `200`

Send only the fields to change: `title`, `description`, `startsAt`, `location`, `maxMembers`, `joinPolicy`. `categoryId` and `mode` cannot change. `maxMembers` cannot go below the current `memberCount`. Members get a `ROOM_UPDATED` system message.

### `DELETE /api/v1/rooms/{roomId}` — Cancel (host only) → `200`

Status becomes `CANCELED`; pending join requests are declined; members get a system message. Data is kept.

### `GET /api/v1/me/rooms?role=HOST|MEMBER` — My rooms

`role=HOST`: rooms I host. `role=MEMBER`: rooms I joined but do not host. Omit `role` for both. Includes ended rooms, newest `startsAt` first. Response: list of `RoomSummary` (page style).

### Errors

| Code | HTTP | When |
|---|---|---|
| `ROOM_NOT_FOUND` | 404 | no such room |
| `ROOM_CATEGORY_NOT_FOUND` | 400 | bad `categoryId` |
| `ROOM_START_IN_PAST` | 400 | `startsAt` is not in the future |
| `ROOM_NICKNAME_REQUIRED` | 400 | anonymous room without nickname |
| `ROOM_NOT_HOST` | 403 | only the host may do this |
| `ROOM_CAPACITY_TOO_SMALL` | 409 | new `maxMembers` < `memberCount` |
| `ROOM_CLOSED` | 409 | room is `ENDED` or `CANCELED` |

---

## 3. Membership & join requests

```kotlin
@Serializable
data class MemberView(
    val memberId: Long,       // id inside this room; use it for keys and reports
    val userId: Long?,        // null in anonymous rooms (except for yourself)
    val displayName: String,  // real name, or nickname in anonymous rooms
    val avatarColor: Int,     // anonymous rooms: derived from memberId
    val role: String,         // "HOST" | "MEMBER"
    val isMe: Boolean,
)
```

**Privacy rule for anonymous rooms:** responses never contain another person's `userId`, real name or profile. Only `memberId` + nickname.

### `POST /api/v1/rooms/{roomId}/join`

Body (only needed for anonymous rooms): `{ "nickname": "Kimchi Lover" }`

- `OPEN` room → `201`, you are a member: `{ "myStatus": "MEMBER" }`. Members get a `MEMBER_JOINED` system message.
- `APPROVAL` room → `202`, a request is created: `{ "myStatus": "PENDING", "requestId": 15 }`. The host gets a `JOIN_REQUEST_CREATED` event.

### `DELETE /api/v1/rooms/{roomId}/join-request` — Cancel my pending request → `200`

### `POST /api/v1/rooms/{roomId}/leave` → `200`

Members leave (system message `MEMBER_LEFT`). The host cannot leave — cancel the room instead.

### `GET /api/v1/rooms/{roomId}/members` — Members (members only)

Response: list of `MemberView`, host first.

### `GET /api/v1/rooms/{roomId}/join-requests` — Pending requests (host only)

```json
{ "success": true, "data": [
  { "requestId": 15, "applicant": { "userId": 42, "displayName": "Håkon", "nationality": "NO", "major": "Design", "avatarColor": 2 },
    "nickname": null, "requestedAt": "2026-11-28T03:00:00Z" }
] }
```

In anonymous rooms `applicant` is `null` and only `nickname` is shown.

### `POST /api/v1/rooms/{roomId}/join-requests/{requestId}/accept` → `200`
### `POST /api/v1/rooms/{roomId}/join-requests/{requestId}/decline` → `200`

Accepting adds the member (system message `MEMBER_JOINED`); the applicant gets a `JOIN_REQUEST_DECIDED` event either way.

### Errors

| Code | HTTP | When |
|---|---|---|
| `ROOM_FULL` | 409 | no seats left (also when accepting a request) |
| `ROOM_CLOSED` | 409 | room is `ENDED` or `CANCELED` |
| `ROOM_ALREADY_MEMBER` | 409 | already a member |
| `ROOM_REQUEST_ALREADY_PENDING` | 409 | already requested |
| `ROOM_NICKNAME_REQUIRED` | 400 | anonymous room without nickname |
| `ROOM_NICKNAME_TAKEN` | 409 | nickname used by another member of this room |
| `ROOM_NOT_MEMBER` | 403 | members-only action |
| `ROOM_NOT_HOST` | 403 | host-only action |
| `ROOM_HOST_CANNOT_LEAVE` | 409 | host tried to leave |
| `ROOM_REQUEST_NOT_FOUND` | 404 | no such pending request |

---

## 4. Chat messages, My chats

The same endpoints work for group rooms and 1:1 chats (a 1:1 chat is a room of type `DIRECT`).

```kotlin
@Serializable
data class Message(
    val id: Long,
    val roomId: Long,
    val type: String,            // "TEXT" | "SYSTEM"
    val sender: MemberView?,     // null for SYSTEM
    val content: String,         // TEXT: the message. SYSTEM: the name of the member involved
    val systemEvent: String?,    // SYSTEM only: "MEMBER_JOINED" | "MEMBER_LEFT" | "ROOM_UPDATED" | "ROOM_CANCELED"
    val createdAt: String,
)
```

System messages carry an event code instead of a sentence so the app can show it in the user's language, e.g. `MEMBER_JOINED` + `"Håkon"` → "Håkon joined" / "Håkon님이 들어왔어요".

### `GET /api/v1/rooms/{roomId}/messages?before=&size=` — History (members only)

Newest first, before-ID paging (see conventions). Response: `{ "items": [Message], "hasNext": true }`.

### `POST /api/v1/rooms/{roomId}/messages` — Send → `201`

Body: `{ "content": "See you at 6!" }` (1–1000 characters after trimming). Response: the created `Message`. Every member, including the sender's other devices, also receives it as a `MESSAGE_CREATED` event.

### `POST /api/v1/rooms/{roomId}/read` — Mark as read → `200`

Body: `{ "lastReadMessageId": 1234 }`. Call when the chat screen shows the newest message.

### `GET /api/v1/me/chats` — My chats

All rooms I am a member of (group and 1:1), most recent activity first.

```kotlin
@Serializable
data class ChatSummary(
    val roomId: Long,
    val type: String,                // "GROUP" | "DIRECT"
    val title: String,               // GROUP: room title. DIRECT: the other person's name
    val avatarColor: Int,            // DIRECT: the other person's color. GROUP: category color index
    val categoryCode: String?,       // GROUP only
    val startsAt: String?,           // GROUP only
    val status: String,              // room status (OPEN / FULL / ENDED / CANCELED)
    val lastMessage: Message?,
    val unreadCount: Int,
)
```

### Errors

| Code | HTTP | When |
|---|---|---|
| `ROOM_NOT_FOUND` | 404 | |
| `ROOM_NOT_MEMBER` | 403 | not a member |
| `CHAT_ROOM_READ_ONLY` | 409 | room is `CANCELED` |
| `INVALID_INPUT` | 400 | empty or too long message |

---

## 5. Real-time events (WebSocket)

Connect once after login and keep the connection while the app is in the foreground:

```
ws://<host>/ws?token=<accessToken>
```

- With OkHttp: `client.newWebSocket(Request.Builder().url(url).build(), listener)`, `pingInterval(30, SECONDS)`.
- The server **only sends**. Clients send messages through the REST endpoint above.
- An invalid/expired token closes the socket with code `4401` → go to login.
- After reconnecting, reload the open chat's history (events sent while offline are not replayed).

Every event is one JSON object with a `type`:

```json
{ "type": "MESSAGE_CREATED", "roomId": 7, "message": { "id": 1235, "roomId": 7, "type": "TEXT", "sender": { "memberId": 3, "userId": 42, "displayName": "Håkon", "avatarColor": 2, "role": "MEMBER", "isMe": false }, "content": "See you at 6!", "systemEvent": null, "createdAt": "2026-12-03T08:01:00Z" } }
{ "type": "ROOM_UPDATED", "roomId": 7 }
{ "type": "JOIN_REQUEST_CREATED", "roomId": 7, "requestId": 15 }
{ "type": "JOIN_REQUEST_DECIDED", "roomId": 7, "accepted": true }
{ "type": "KNOCK_RECEIVED", "knockId": 3 }
{ "type": "KNOCK_ACCEPTED", "knockId": 3, "roomId": 12 }
```

`isMe` is computed for each receiver. On `ROOM_UPDATED`, re-fetch the room. Unknown `type` values must be ignored (new ones may be added).

---

## 6. Knocks & friends

A **knock** is a friend request with a short message (Figma "Send a friendly Knock"). Accepting it makes both users friends and opens a 1:1 chat. Knocks always use real names.

```kotlin
@Serializable
data class Knock(
    val id: Long,
    val from: UserSummary,
    val to: UserSummary,
    val message: String,
    val status: String,     // "PENDING" | "ACCEPTED" | "DECLINED" | "CANCELED"
    val roomId: Long?,      // the 1:1 chat, once accepted
    val createdAt: String,
)
```

### `POST /api/v1/knocks` → `201`

Body: `{ "toUserId": 42, "message": "Hi! I saw you like hiking too 🙂" }` (1–200 characters). Response: `Knock`. The receiver gets a `KNOCK_RECEIVED` event.

### `GET /api/v1/knocks?box=received|sent`

Pending knocks I received (default) or sent, newest first. Response: list of `Knock`.

### `POST /api/v1/knocks/{knockId}/accept` → `200`

Receiver only. Response: `{ "roomId": 12 }`. The sender gets a `KNOCK_ACCEPTED` event.

### `POST /api/v1/knocks/{knockId}/decline` → `200` (receiver) · `DELETE /api/v1/knocks/{knockId}` → `200` (sender cancels)

### `GET /api/v1/friends`

```json
{ "success": true, "data": [
  { "user": { "userId": 42, "displayName": "Håkon", "nationality": "NO", "major": "Design", "avatarColor": 2 },
    "roomId": 12, "since": "2026-12-05T10:00:00Z" }
] }
```

### `GET /api/v1/friends/recommendations?size=20` — Find friends → Recommended

People I am not friends with and have no pending knock with, best match first. The score is computed by our own algorithm (shared interests and languages).

```json
{ "success": true, "data": [
  { "user": { "userId": 51, "displayName": "Minji", "nationality": "KR", "major": "Software", "avatarColor": 3 },
    "sharedInterestIds": [1, 13], "score": 0.62 }
] }
```

### Errors

| Code | HTTP | When |
|---|---|---|
| `KNOCK_SELF` | 400 | knocking yourself |
| `KNOCK_TARGET_NOT_FOUND` | 404 | no such approved user |
| `KNOCK_ALREADY_PENDING` | 409 | a pending knock exists between the two (either direction) |
| `KNOCK_ALREADY_FRIENDS` | 409 | already friends — open the existing 1:1 chat |
| `KNOCK_NOT_FOUND` | 404 | no such pending knock |
| `KNOCK_NOT_RECEIVER` / `KNOCK_NOT_SENDER` | 403 | wrong side for this action |

---

## 7. Reports

### `POST /api/v1/reports` → `201`

```json
{ "targetType": "MESSAGE", "targetId": 1235, "reason": "HARASSMENT", "detail": "Optional, up to 500 characters" }
```

| Field | Values |
|---|---|
| `targetType` | `ROOM`, `MESSAGE`, `USER` (in anonymous rooms report the `MESSAGE`; the server knows who sent it) |
| `reason` | `SPAM`, `HARASSMENT`, `INAPPROPRIATE`, `OTHER` |

Response: `{ "reportId": 9 }`.

| Code | HTTP | When |
|---|---|---|
| `REPORT_TARGET_NOT_FOUND` | 404 | target does not exist or you cannot see it |
| `REPORT_DUPLICATE` | 409 | you already reported this target |

Admin review of reports is out of scope for the deadline (see Team Guide §3).
