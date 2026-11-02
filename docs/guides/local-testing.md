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

> 메뉴 위치는 Android Studio 버전마다 조금씩 다릅니다. 새 화면(새 UI)에서는 왼쪽 위 **≡ 아이콘** 안에 File, Tools 같은 메뉴가 있습니다. 설정 창은 어디서든 `Ctrl + Alt + S`로 열립니다.

### 1) Android Studio 설치 (처음 한 번)
1. developer.android.com/studio에서 Windows용 설치 파일을 받아 실행합니다.
2. 구성 요소 선택 화면에서 **Android Studio**와 **Android Virtual Device**가 둘 다 체크되어 있는지 확인합니다.
3. 처음 실행하면 설정 마법사가 뜹니다.
   - 이전 설정 가져오기: **Do not import settings**(가져오지 않음)
   - 설치 유형: **Standard**
   - 라이선스 화면: 왼쪽 목록 항목을 하나씩 눌러 **모두 Accept** → Finish
   - SDK를 내려받는 데 몇 분 걸리고, 끝나면 시작 화면이 나옵니다.

### 2) 프로젝트 열기
1. 시작 화면에서 **Open**을 누릅니다. (다른 프로젝트가 열려 있으면 File → Open)
2. `C:\dev\campus-hangout\android`를 고르고 OK를 누릅니다. **최상위 `campus-hangout`이 아니라 그 안의 `android` 폴더**입니다.
3. 신뢰 여부를 물으면 **Trust Project**를 누릅니다.
4. 자동으로 Gradle 동기화(Sync)가 시작됩니다. 오른쪽 아래 상태 표시줄에 진행 상황이 나오고, 처음에는 5~10분 걸립니다.

### 3) Gradle JDK를 21로 맞추기 (중요)
서버에서처럼 Gradle이 21보다 새로운 자바로 돌면 동기화가 실패합니다. 프로젝트를 처음 열었을 때 한 번 확인합니다.
1. `Ctrl + Alt + S` → **Build, Execution, Deployment → Build Tools → Gradle**
2. **Gradle JDK** 목록에서 버전이 **21**인 항목을 고릅니다. (예: `jbr-21`, `temurin-21`)
   - 21이 없으면 **Add JDK from disk...** → `C:\Program Files\Eclipse Adoptium\jdk-21...` 폴더 선택
3. OK → 오른쪽 위 **코끼리 아이콘(Sync Project with Gradle Files)** 또는 File → Sync Project with Gradle Files로 다시 동기화합니다.

**성공 확인**: 아래쪽 **Build** 창(망치 아이콘)의 Sync 탭에 빨간 오류 없이 초록 체크가 보이면 됩니다.

| 동기화 중 나오는 것 | 할 일 |
|---|---|
| Android Gradle Plugin 업그레이드 제안 | 닫거나 "Remind me tomorrow". 업그레이드하지 않습니다 |
| `26.0.2.1`처럼 숫자만 있는 오류 | 위 3)에서 Gradle JDK를 21로 바꾸고 다시 동기화 |
| API 36 SDK가 없다는 오류 | 오류 안의 설치 링크를 누르거나, `Ctrl + Alt + S` → Languages & Frameworks → Android SDK → SDK Platforms에서 **Android 16 (API 36)** 체크 → Apply |
| 그 밖의 빨간 오류 | Build 창의 오류 내용을 전부 복사해서 보내 주세요 |

### 4) 실행할 기기 준비

**방법 A: 에뮬레이터 (PC 안의 가상 휴대폰)**
1. **가상화 확인**: 작업 관리자(`Ctrl + Shift + Esc`) → 성능 → CPU에서 **가상화: 사용**인지 봅니다. "사용 안 함"이면 BIOS에서 Intel VT-x 또는 AMD SVM을 켜야 합니다. (연구실 PC면 관리자에게 문의)
2. Windows 검색 → **Windows 기능 켜기/끄기** → **Windows 하이퍼바이저 플랫폼** 체크 → 재부팅 (이미 켜져 있으면 넘어갑니다)
3. Android Studio 오른쪽 세로 막대의 **Device Manager**(휴대폰 아이콘)를 엽니다. 없으면 View → Tool Windows → Device Manager.
4. **+** → **Create Virtual Device**(버전에 따라 Add Device)
5. 기기: Phone 분류에서 **Pixel** 기종 아무거나
6. 시스템 이미지: **API 36 (Android 16)** 또는 **API 35 (Android 15)** 의 **x86_64** 이미지를 다운로드 아이콘으로 받은 뒤 선택
7. Finish → 목록에 생긴 기기의 ▶를 누르면 에뮬레이터 창이 켜집니다. 첫 부팅은 1~3분 걸립니다.
8. **에뮬레이터 기본 언어는 영어**입니다. 한국어로 보려면 에뮬레이터 안에서 Settings → System → Languages → System languages → Add a language → 한국어 → 오른쪽 손잡이(≡)를 끌어 맨 위로 올립니다.

**방법 B: 실제 안드로이드 폰 (Android 8.0 이상)**
1. 설정 → 휴대전화 정보 → 소프트웨어 정보 → **빌드번호**를 7번 누릅니다. "개발자 모드가 켜졌습니다"가 뜹니다.
2. 설정 → **개발자 옵션** → **USB 디버깅** 켜기
3. **데이터 전송이 되는 USB 케이블**로 연결하고, 폰에 뜨는 "USB 디버깅을 허용하시겠습니까?"에서 **허용**
4. Android Studio 위쪽 기기 목록에 폰 이름이 보이면 준비 완료
   - 안 보이면: 케이블 교체, 폰의 USB 연결 방식을 "파일 전송"으로, 삼성 폰은 "Samsung Android USB Driver" 설치

### 5) 앱 실행
1. 위쪽 도구 막대에서 실행 대상이 **app**, 기기가 방금 준비한 에뮬레이터나 폰인지 확인합니다.
2. **▶ Run 'app'** (`Shift + F10`)을 누릅니다.
3. 아래 Build 창에서 빌드가 진행되고(처음 1~3분), 끝나면 기기에서 앱이 자동으로 켜집니다.
4. **앱이 켜지자마자 꺼지면**: 아래쪽 **Logcat** 창 → 위 검색칸에 `package:mine level:error` 입력 → 빨간 `FATAL EXCEPTION` 부분을 복사해서 보내 주세요.

### 6) 화면 확인 체크리스트

| # | 확인할 것 | 하는 방법 | 기대 결과 |
|---|---|---|---|
| 1 | 첫 화면 | 앱 실행 직후 | 제목 "채팅방 둘러보기"(영어: Explore rooms), 가운데 말풍선 아이콘과 "아직 채팅방이 없어요"(No rooms yet), 하단 탭 4개 중 **홈**이 파란색 |
| 2 | 카테고리 칩 | 제목 아래 칩 줄을 왼쪽으로 밀기 | 전체·취미·스터디·언어교환·맛집·여행·운동·K-컬처·학교생활 9개, 처음엔 "전체"가 연한 파란 배경 |
| 3 | 칩 선택 | "여행" 누르기 | 연한 파란 배경이 "여행"으로 옮겨감 |
| 4 | 탭 이동 | 채팅 → 친구 → 마이 차례로 누르기 | 제목이 "내 채팅", "친구", "마이페이지"로 바뀌고, 누른 탭만 파란색 |
| 5 | 탭 상태 유지 | "여행" 선택 → 친구 탭 → 다시 홈 | "여행"이 계속 선택되어 있음 |
| 6 | 뒤로 가기 | 친구 탭에서 뒤로 가기 | 홈으로 돌아옴. 홈에서 한 번 더 누르면 앱이 닫힘 |
| 7 | 화면 회전 | 에뮬레이터 오른쪽 막대의 회전 버튼 (화면이 안 돌면 하단에 뜨는 회전 아이콘을 누르거나 빠른 설정에서 자동 회전 켜기) | 가로 화면에서도 깨지지 않고 "여행" 선택이 유지됨 |
| 8 | 다크 테마 | 설정 → 디스플레이 → 다크 테마 켜기 | 배경이 거의 검정, 글자는 밝은색, 선택된 탭은 연한 파란색 |
| 9 | 언어 | 시스템 언어를 영어 ↔ 한국어로 바꾸기 | 모든 문구가 해당 언어로 바뀜. 앱 이름은 둘 다 "Campus Hangout" |
| 10 | 앱 아이콘 | 기기의 앱 목록 | 파란 원 안에 흰 말풍선 두 개, 이름 "Campus Hangout" |
| 11 | 화면 가장자리 | 맨 위 상태 표시줄, 맨 아래 제스처 막대 주변 | 제목과 탭이 가려지거나 겹치지 않음 |

### 7) 자동 테스트와 린트
**단위 테스트 (2개)**
1. 왼쪽 프로젝트 창 위쪽의 보기 방식을 **Android**로 둡니다.
2. app → kotlin+java(또는 java) → **com.campushangout.android (test)** → navigation → **TopLevelDestinationTest**
3. 마우스 오른쪽 → **Run 'TopLevelDestinationTest'**
4. 아래 Run 창에 **Tests passed: 2**가 나오면 통과입니다.

**단위 테스트 + 린트를 명령으로 한 번에**
아래쪽 **Terminal** 탭(PowerShell, `android` 폴더에서 열림)에서 실행합니다. 이 창에도 JDK 21 설정이 필요합니다.
**Android Studio의 동기화·빌드가 완전히 끝난 뒤에** 실행하세요. 둘이 동시에 돌면 SDK 설치가 서로 부딪혀 실패할 수 있습니다.
```powershell
$env:JAVA_HOME = (Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-21*" | Select-Object -First 1).FullName
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat testDebugUnitTest lintDebug
```
- `BUILD SUCCESSFUL`이면 통과입니다.
- 린트 보고서: `android\app\build\reports\lint-results-debug.html` (경고는 실패가 아닙니다)

### 8) 결과 알려 주기
6)의 표 번호별로 통과/실패를 알려 주세요. 실패한 항목은 화면 캡처나 오류 문구를 같이 보내 주세요.

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

### `Failed to install the following SDK components: platforms;android-36` (SDK 설치 실패)
함께 보이는 문구: `FileAlreadyExistsException ... Sdk\.temp\PackageOperation01\...`

**원인**
- 앱은 Android 16(API 36) SDK로 빌드하는데, 이 SDK가 아직 설치되지 않은 상태였습니다.
- 그래서 명령줄 빌드가 직접 내려받으려다, SDK 임시 폴더(`Sdk\.temp`)에 이전 설치 파일이 남아 있어 실패했습니다.
- 임시 파일은 Android Studio가 같은 SDK를 동시에 설치하고 있었거나, 이전 설치가 중간에 끊긴 경우에 남습니다.

**해결**
1. Android Studio를 닫고, PowerShell에서 Gradle 백그라운드 프로세스를 끕니다.
   ```powershell
   cd C:\dev\campus-hangout\android
   .\gradlew.bat --stop
   ```
2. 남은 임시 폴더를 지웁니다.
   ```powershell
   Remove-Item -Recurse -Force "$env:LOCALAPPDATA\Android\Sdk\.temp"
   ```
3. Android Studio를 다시 열고 `Ctrl + Alt + S` → Languages & Frameworks → Android SDK → **SDK Platforms** 탭에서 **API Level 36 (Android 16)** 을 체크 → Apply → 설치가 끝나면 Finish
4. 설치를 확인합니다. 목록에 `android-36`이 보이면 됩니다.
   ```powershell
   Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk\platforms"
   ```
5. Android Studio에서 다시 동기화(코끼리 아이콘)하고, 끝난 뒤에 명령줄 테스트를 실행합니다.

### `This version only understands SDK XML versions up to 3 but an SDK XML file of version 4 was encountered`
- 경고일 뿐 오류가 아닙니다. 설치한 Android Studio가 프로젝트의 Android Gradle Plugin(8.13)보다 새 버전이라서 나옵니다. 빌드 결과에는 영향이 없습니다.
- 다만 이 때문에 명령줄 빌드가 SDK를 직접 설치하는 과정은 불안정할 수 있습니다. **SDK는 항상 Android Studio의 SDK Manager로 설치**하세요.

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
  - [ ] D의 6) 체크리스트 11개 항목
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
