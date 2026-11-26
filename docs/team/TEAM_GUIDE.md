# Campus Hangout — Team Guide

The single page every team member should read. GitHub is our shared workspace, so **everything on GitHub (PRs, issues, commit messages, team docs, API specs) is written in English.**

Deadline: **Thursday, December 10, 2026.** Two weeks. Scope is cut to what we can finish well (see §3).

---

## 1. Who owns what

| Member | GitHub | Owns |
|---|---|---|
| Hyeonsu | [@usernamehs](https://github.com/usernamehs) | **Backend – account**: sign-up, login (JWT), student ID verification, admin review, profile, languages, interests |
| Yechan | [@pantajia12](https://github.com/pantajia12) | **Backend – social**: meetup rooms, group chat (real-time), knocks & friends, reports. **Android**: network layer and every screen that is *not* in Figma (admin screens, etc.) |
| Håkon | (GitHub username to be added) | **Design & Android UI**: Figma, and every screen that *is* in Figma |

Shared (everyone may add to it, nobody rewrites it alone): `server/.../common`, `server/.../contract`, the design system (`android/.../designsystem`), CI, docs.

## 2. Two-week plan

| Dates | Hyeonsu (account) | Yechan (social) | Håkon (UI) |
|---|---|---|---|
| **Nov 26–27** | Merge PR #1, #2 | Team docs + social API spec (this PR), common code PR | Read §6 and [`docs/api/social.md`](../api/social.md); start screens with fake data |
| **Nov 28–Dec 1** | Issue: move APIs to `/api/v1`, specific error codes, public `ping`/`health` | Meetup rooms API (create, explore, detail, join, approval, leave) | Sign-up/login flow, Explore, Create room, Room details |
| **Dec 2–4** | Admin review API (approve/reject, roles), re-submit after rejection | Group chat (REST + WebSocket), My chats | Chat screens, My chats, My campus / Edit profile |
| **Dec 5–6** | Public profile API, data for friend recommendations | Knocks, friends, 1:1 chat, reports, recommendations; deploy server | Find friends, Knock sheets, profile screens |
| **Dec 7–8** | Integration and bug fixes (everyone): full flow on two phones, demo data | | |
| **Dec 9** | Feature freeze, README, presentation, tag `v1.0.0` | | |
| **Dec 10** | Buffer / submission | | |

## 3. Scope

**Must (course requirements):** sign-up with student ID + admin approval, login, profile + interests editing, create/join category meetup rooms, real-time group chat, data CRUD, GitHub history.

**Should:** anonymous rooms (nickname), host approval for joining, knocks → 1:1 chat, friend recommendations, reports.

**Cut (unless everything else is done):** room tags, photo/location messages, push notifications, blocking, admin UI for reports, sticker board, email verification, password reset, translation.

## 4. Workflow

1. Create a branch from the latest `main`: `feat/rooms-api`, `fix/chat-reconnect`, `docs/api-spec`, `ui/explore-screen`.
2. Commit small, logical steps (format below).
3. Push the branch and open a **Pull Request into `main`**. Fill in the PR template, including *how to test*.
4. **One approval from another member** + green CI → **Squash and merge**. Squash merging puts the PR author's GitHub account on the commit, so everyone's work shows up in the contributors list.
5. Delete the branch after merging. Never push directly to `main`.

Keep PRs small (ideally under ~400 changed lines) so reviews take minutes, not days.

**Stay up to date** (every day before you start):

```bash
git switch <your-branch>
git fetch origin
git merge origin/main
```

**Commit messages** — [Conventional Commits](https://www.conventionalcommits.org/), English, imperative mood:

```
type(scope): short summary in imperative mood      (max ~60 chars, no period)

Optional body: what changed and why.
```

- `type`: `feat` · `fix` · `refactor` · `test` · `docs` · `chore` · `ci`
- `scope`: `account` · `social` · `ui` · `common` · `infra`
- Examples: `feat(social): add meetup room creation API`, `feat(ui): add Explore screen with category chips`, `fix(account): allow re-submission after rejection`
- The PR title uses the same format — it becomes the squash commit message.

**Make sure GitHub knows your commits are yours:** the email in `git config user.email` must be listed (and verified) under GitHub → Settings → Emails. Otherwise your commits do not count as contributions.

## 5. Backend rules

**Packages are the module boundaries** (one Gradle module, checked by a test in CI):

```
server/src/main/kotlin/com/campushangout/server/
├── common/     shared: response envelope, errors, @CurrentUser, utilities   (everyone)
├── contract/   interfaces one area offers to the other (e.g. UserDirectory)  (both backend devs approve)
├── account/    sign-up, login, JWT, student verification, admin review       (Hyeonsu)
├── profile/    profile, languages, interests                                 (Hyeonsu)
└── social/     rooms, chat, knocks, friends, reports                         (Yechan)
```

- `social` never imports `account` or `profile`, and the other way round. Cross-area needs go through an interface in `contract` (for example, social asks `UserDirectory` for display names instead of reading the `user_profiles` table).
- **Tables have one owner.** Never query or join another area's tables. Columns that point into another area (e.g. `room_members.user_id`) have **no foreign key**, just an index.
- **Migrations:** new files are named with a timestamp so two people never pick the same number: `V202611281530__social_create_rooms.sql` (year-month-day-hour-minute). Never edit a migration that is already on `main`; add a new one.
- **Errors:** each area has its own error-code enum implementing `ApiErrorCode`, with a prefix (`AUTH_`, `VERIFY_`, `PROFILE_`, `ADMIN_` / `ROOM_`, `CHAT_`, `KNOCK_`, `FRIEND_`, `REPORT_`). The app switches on `error.code`, never on the message.
- **Current user:** controllers take `@CurrentUser user: AuthUser` instead of reading the security context.
- Every endpoint is documented in `docs/api/` **before or in the same PR** as its implementation.

## 6. Android rules

- **Screens follow Figma.** Håkon builds them with Jetpack Compose + Material 3 using the existing theme and `Ch*` components.
- **Screens never call the network directly.** Each feature has a `Repository` interface; screens and ViewModels depend only on it.
  - Start with a **fake** implementation that returns hard-coded data shaped exactly like the API spec, so UI work never waits for the backend.
  - The **real** implementation (Retrofit) is added later by Yechan and swapped in at one place.
- Models mirror the API JSON (see the Kotlin snippets in [`docs/api/social.md`](../api/social.md)). Dates arrive as ISO-8601 UTC strings; convert to local time only for display.
- **No hard-coded UI text:** put every string in `res/values/strings.xml` (English) and `res/values-ko/strings.xml` (Korean).
- Package per feature: `feature/explore`, `feature/room`, `feature/chat`, `feature/friends`, `feature/mycampus`, `feature/auth`, `feature/admin`.

## 7. API documents

- [`docs/api/conventions.md`](../api/conventions.md) — envelope, errors, auth, time, paging (read first)
- [`docs/api/social.md`](../api/social.md) — rooms, chat, knocks, friends, reports (Yechan)
- `docs/api/account.md`, `docs/api/profile.md` — account and profile (Hyeonsu)

## 8. Definition of done (for every PR)

- [ ] Builds and all tests pass locally and in CI
- [ ] New/changed endpoints are in `docs/api/`
- [ ] No secrets, no hard-coded UI strings, no cross-area imports
- [ ] The PR description says how a reviewer can test it
