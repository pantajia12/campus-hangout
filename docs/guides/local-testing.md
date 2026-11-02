# 로컬 테스트 가이드 (push 전에 확인)

GitHub에 올리기 전에 내 컴퓨터에서 서버와 앱이 제대로 도는지 확인하는 방법입니다.
**A. 처음 한 번만 준비**는 한 번 해 두면 되고, 매번 할 일은 **B. 압축 풀기**부터입니다.
Phase마다 새로 확인할 항목은 맨 아래 **Phase별 체크리스트**에 추가합니다.

---

## A. 처음 한 번만 준비

| 프로그램 | 용도 | 받는 곳 |
|---|---|---|
| Android Studio (최신 안정 버전) | 앱 실행, 에뮬레이터 | developer.android.com/studio |
| JDK 21 (Eclipse Temurin 21) | 서버 실행 | adoptium.net |
| Git | GitHub에 올리기 (Windows는 Git Bash 포함) | git-scm.com |
| Docker Desktop | 로컬 DB (Phase 1부터 필수) | docker.com/products/docker-desktop |

### Android Studio
- 설치 마법사에서 **Standard** 설치를 고르면 Android SDK와 에뮬레이터가 같이 설치됩니다.

### JDK 21
- **반드시 21 버전**을 받습니다. 최신 버전(25, 26 등)으로 Gradle을 실행하면 빌드가 실패합니다. (아래 "자주 나는 오류" 참고)
- **Windows**: `.msi` 설치 파일을 실행하고, 설치 옵션에서 **"Set JAVA_HOME variable"** 항목을 "설치"로 바꿉니다.
- **Mac**: `.pkg` 설치 파일을 실행합니다.
- 설치 후 새 터미널을 열고 아래 명령으로 확인합니다. `21`이 보이면 됩니다.
  ```bash
  java -version
  ```

---

## B. 압축 풀기

**경로 규칙**
- 한글·공백이 없는 짧은 경로에 풉니다. Android 빌드는 경로에 한글이 있으면 실패할 수 있고, Windows는 경로가 길면 오류가 납니다.
- 바탕화면, 문서처럼 OneDrive·iCloud가 동기화하는 폴더는 피합니다. 빌드 중 파일이 잠겨 오류가 날 수 있습니다.

### Windows
1. 파일 탐색기에서 `C:\dev` 폴더를 만듭니다.
2. 압축 파일을 마우스 오른쪽으로 누르고 **"압축 풀기..."** 를 고릅니다.
3. 대상 폴더를 `C:\dev`로 바꾸고 압축을 풉니다.
4. `C:\dev\campus-hangout` 안에 `android`, `server`, `docs`, `infra` 폴더가 보이면 정상입니다.
   - `C:\dev\campus-hangout-v0.0.3\campus-hangout`처럼 한 단계 더 들어가 있으면, 안쪽 `campus-hangout` 폴더를 `C:\dev`로 옮깁니다.

### Mac
1. Finder에서 홈 폴더에 `dev` 폴더를 만듭니다.
2. 압축 파일을 더블클릭해서 풀고, 생긴 `campus-hangout` 폴더를 `~/dev`로 옮깁니다.

> 이전 버전 폴더가 이미 있으면 지우거나 이름을 바꾼 뒤 새로 풉니다. 이미 GitHub에 올린 뒤라면 압축 파일 대신 번들 파일로 받습니다(아래 E 참고).

---

## C. 서버 테스트

### 1) 자동 테스트
**Windows**: 시작 메뉴에서 "PowerShell"을 엽니다.
```powershell
cd C:\dev\campus-hangout\server
.\gradlew.bat test
```
**Mac**: 터미널을 엽니다.
```bash
cd ~/dev/campus-hangout/server
./gradlew test
```
- 처음에는 Gradle과 라이브러리를 내려받느라 3~10분 걸립니다.
- 마지막에 `BUILD SUCCESSFUL`이 나오면 통과입니다.
- 결과 보고서는 `server/build/reports/tests/test/index.html`입니다. 브라우저로 열면 테스트 목록과 성공/실패가 보입니다.

### 2) 직접 실행
```powershell
.\gradlew.bat bootRun      # Mac은 ./gradlew bootRun
```
- 로그에 `Started CampusHangoutServerApplication`이 보이면 실행된 것입니다.
- 아래쪽에 `EXECUTING`이 표시된 채 멈춰 있는 게 정상입니다. 서버가 켜져 있는 상태입니다.

브라우저에서 아래 주소를 엽니다.

| 주소 | 기대 결과 |
|---|---|
| http://localhost:8080/api/v1/ping | `{"success":true,"data":{"status":"ok","serverTime":"..."}}` |
| http://localhost:8080/actuator/health | `{"status":"UP", ...}` |
| http://localhost:8080/api/v1/없는주소 | `{"success":false,"error":{"code":"NOT_FOUND", ...}}` |

- 확인이 끝나면 터미널에서 `Ctrl + C`로 서버를 끕니다.

---

## D. 앱 테스트 (Android Studio)

### 1) 프로젝트 열기
1. Android Studio를 실행하고 **Open**을 누릅니다.
2. **`campus-hangout` 안의 `android` 폴더**를 고릅니다. 최상위 `campus-hangout` 폴더가 아닙니다.
3. 프로젝트를 신뢰할지 물으면 **Trust Project**를 누릅니다.
4. 오른쪽 아래에 Gradle 동기화 진행 표시가 나옵니다. 처음에는 5~10분 걸립니다.

동기화 중에 나올 수 있는 것

| 화면 | 할 일 |
|---|---|
| Android Gradle Plugin 업그레이드 제안 | 닫습니다. 버전은 Phase가 끝날 때 한꺼번에 올립니다 |
| SDK(API 36)가 없다는 오류 | 오류 메시지 안의 설치 링크를 누르거나, Tools → SDK Manager에서 **Android 16 (API 36)** 을 체크하고 설치 |

### 2) 실행할 기기 준비 (둘 중 하나)
**에뮬레이터**
1. 오른쪽 세로 막대의 **Device Manager**(휴대폰 아이콘)를 엽니다.
2. **+** → **Create Virtual Device** → Pixel 기종을 고릅니다.
3. 시스템 이미지는 API 35 또는 36을 내려받아 고르고 Finish를 누릅니다.
4. 에뮬레이터가 켜지지 않으면 BIOS의 가상화 기능(Intel VT-x / AMD-V)이 켜져 있는지 확인합니다.

**실제 휴대폰** (Android 8.0 이상)
1. 설정 → 휴대전화 정보 → 소프트웨어 정보 → 빌드번호를 7번 눌러 개발자 옵션을 켭니다.
2. 개발자 옵션에서 **USB 디버깅**을 켭니다.
3. USB로 연결하고, 휴대폰에 뜨는 허용 창에서 확인을 누릅니다.

### 3) 실행
상단 도구 막대에서 실행 대상이 `app`인지, 기기가 맞는지 확인하고 ▶(Run, `Shift + F10`)를 누릅니다.

### 4) 자동 테스트
- 왼쪽 프로젝트 창에서 `app/src/test/java/.../navigation/TopLevelDestinationTest`를 마우스 오른쪽으로 누르고 **Run**을 고릅니다.
- 아래 실행 창에 테스트가 모두 초록색이면 통과입니다.
- 린트 검사는 아래쪽 **Terminal** 탭에서 실행합니다. `BUILD SUCCESSFUL`이 나오면 통과입니다.
  ```powershell
  .\gradlew.bat lintDebug    # Mac은 ./gradlew lintDebug
  ```

---

## E. 다음 Phase부터 (이미 GitHub에 올린 뒤)

압축 파일 대신 **번들 파일**(새 커밋만 묶은 파일)로 받습니다.
1. 번들 파일을 `C:\dev`(Mac은 `~/dev`)에 둡니다.
2. 아래 명령으로 새 커밋을 가져옵니다. Windows는 PowerShell에서 실행합니다.
   ```bash
   cd C:\dev\campus-hangout        # Mac은 cd ~/dev/campus-hangout
   git pull --tags ../번들파일이름.bundle main   # --tags: 새 버전 태그도 같이 가져옴
   git log --oneline -3                          # 맨 위에 새 커밋이 보이면 성공
   ```
3. 위 C, D를 다시 해서 테스트합니다.
4. 모두 통과하면 그때 올립니다.
   ```bash
   git push
   git push origin --tags
   ```

테스트가 실패하면 올리지 말고, 오류 메시지나 화면 캡처를 보내 주세요.

---

## 자주 나는 오류

### `What went wrong:` 아래에 `26.0.2.1`처럼 숫자만 나올 때
**원인**: Gradle이 JDK 21이 아닌 더 새로운 자바(25, 26 등)로 실행되었습니다. 지금 쓰는 Gradle 8.14는 자바 24까지만 실행을 지원하고, 새 버전 번호를 읽지 못해 그 번호만 출력하고 멈춥니다.

**해결** (Windows PowerShell)
1. 지금 쓰이는 자바를 확인합니다.
   ```powershell
   java -version
   echo $env:JAVA_HOME
   where.exe java
   ```
2. JDK 21이 없으면 adoptium.net에서 Temurin **21** 설치 파일(.msi)을 받아 설치합니다.
3. 지금 열린 창에서만 JDK 21을 쓰도록 바꿉니다.
   ```powershell
   $env:JAVA_HOME = (Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-21*" | Select-Object -First 1).FullName
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   java -version
   ```
4. 이전 자바로 켜진 Gradle을 끄고 다시 실행합니다.
   ```powershell
   .\gradlew.bat --stop
   .\gradlew.bat test
   ```
5. 매번 3번을 하기 싫으면, Windows 검색에서 "시스템 환경 변수 편집"을 열고 **환경 변수**로 들어갑니다.
   - **사용자 변수**와 **시스템 변수** 양쪽의 `JAVA_HOME`을 JDK 21 폴더로 바꿉니다. 사용자 변수가 시스템 변수보다 우선합니다.
   - 바꾼 뒤에는 PowerShell을 새로 엽니다.
   - 공용 PC라서 전역 설정을 바꾸기 곤란하면 3번 방법을 계속 씁니다.

### 터미널에 테스트 이름이 `?덉긽?섏?`처럼 깨져 보일 때
**원인**: 테스트 이름(한글)은 UTF-8로 출력되는데, PowerShell 창은 한국어 Windows 기본 인코딩(CP949)으로 읽어서 생기는 표시 문제입니다. 테스트 결과에는 영향이 없습니다.

**해결**: 테스트 전에 아래 명령을 한 번 실행합니다. 또는 결과 보고서(`build/reports/tests/test/index.html`)를 브라우저로 열면 한글이 정상으로 보입니다.
```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

### 테스트가 실패했을 때 원인 찾기
1. 결과 보고서 `build/reports/tests/test/index.html`을 브라우저로 엽니다.
2. 실패한 테스트를 누르면 **어느 줄에서, 무엇을 기대했는데 무엇이 나왔는지**가 보입니다. 예: `Status expected:<409> but was:<404>`
3. 그 내용을 복사해서 보내 주세요.

---

## Phase별 체크리스트

### Phase 0 (v0.0.x)
- 서버
  - [ ] `test` 통과 (8개)
  - [ ] ping, health, 없는 주소 응답이 위 표와 같음
- 앱 화면
  - [ ] 앱 이름 "Campus Hangout", 파란 말풍선 아이콘
  - [ ] 하단 탭 4개. 휴대폰 언어가 한국어면 홈·채팅·친구·마이, 영어면 Home·Chats·Friends·My
  - [ ] 탭을 누르면 화면이 바뀌고, 선택한 탭이 파란색으로 표시됨
- 홈 카테고리 칩
  - [ ] 좌우로 스크롤되고, 누르면 선택 표시가 옮겨감
  - [ ] 칩을 고른 뒤 다른 탭에 갔다 돌아와도 선택이 유지됨
  - [ ] 화면을 회전해도 선택이 유지됨
- 설정 바꿔 보기
  - [ ] 다크 테마: 설정 → 디스플레이 → 다크 테마를 켜면 어두운 색으로 바뀜
  - [ ] 언어: 설정 → 시스템 → 언어에서 English를 맨 위로 올리면 문구가 영어로 바뀜
- 앱 자동 테스트
  - [ ] 단위 테스트 2개 통과
  - [ ] `lintDebug` 통과
- (선택) 로컬 DB
  - [ ] `infra` 폴더에서 아래 순서로 실행했을 때 `docker compose ps`에 `(healthy)`가 2개 보임
  ```powershell
  cd C:\dev\campus-hangout\infra
  copy .env.example .env        # Mac은 cp .env.example .env
  docker compose up -d
  docker compose ps
  docker compose down
  ```
