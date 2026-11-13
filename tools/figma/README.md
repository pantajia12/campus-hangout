# Figma 데이터 내보내기

Figma 디자인의 **정확한 색 값, 화면 이름, 문구**를 개발에 쓰기 위한 도구입니다.
PNG 캡처만으로는 색 값(#RRGGBB)을 정확히 읽을 수 없어서 만들었습니다.

결과물은 `tools/figma/figma-export.zip` 하나이고, 이 파일을 대화창에 올리면 됩니다.
토큰은 결과물에 들어가지 않습니다.

## 무엇을 받나요

| 파일 | 내용 | API 호출 |
|---|---|---|
| `out/file.json` | Figma 파일 전체 데이터 (화면, 문구, 색, 글꼴) | 1회 |
| `out/frames.csv` | 화면 목록 (ID, 페이지, 섹션, 이름, 크기) | 없음 (file.json에서 만듦) |
| `out/frames/*.png` | 화면별 이미지 (`-Images`를 붙였을 때만) | 화면 20개마다 1회 |

> **API 호출 한도 주의**: Figma 무료(Starter) 플랜 파일은 계정 좌석 종류에 따라
> 파일 읽기 API를 **한 달에 몇 번**밖에 못 쓸 수 있습니다. 그래서 이 스크립트는
> 이미 받은 `file.json`이 있으면 다시 받지 않습니다. 기본 실행은 API를 **1회**만 씁니다.

## 1. 토큰 만들기 (처음 한 번)

1. 브라우저에서 Figma에 로그인합니다. (디자인 파일을 볼 수 있는 계정)
2. 왼쪽 위 계정 이름 → **Settings** → **Security** 탭
3. **Personal access tokens** → **Generate new token**
4. 이름: `campus-hangout-export`, 만료: **7일**(가장 짧게)
5. 권한(Scopes): **File content → Read-only** 만 선택 (나머지는 No access)
6. 만들어진 `figd_...` 토큰을 복사합니다. **이 창을 닫으면 다시 볼 수 없습니다.**

> 토큰은 비밀번호와 같습니다. 대화창, GitHub, 파일에 붙여 넣지 마세요.
> 아래처럼 PowerShell 창 안에서만 씁니다. 창을 닫으면 사라집니다.

## 2. 실행

PowerShell을 열고 (시작 → `powershell`) 아래를 한 줄씩 실행합니다.

```powershell
cd C:\dev\campus-hangout

# 토큰을 이 창에서만 쓰는 임시 변수로 설정 (따옴표 안에 붙여 넣기)
$env:FIGMA_TOKEN = "figd_여기에_토큰"

# 기본: file.json + frames.csv (API 1회)
powershell -ExecutionPolicy Bypass -File .\tools\figma\export-figma.ps1
```

마지막에 `완료: C:\dev\campus-hangout\tools\figma\figma-export.zip`이 나오면 성공입니다.

### 화면 이미지도 필요할 때

```powershell
# 전체 화면 PNG (화면이 많으면 API를 여러 번 씀)
powershell -ExecutionPolicy Bypass -File .\tools\figma\export-figma.ps1 -Images

# 특정 화면만 (frames.csv의 Id 열 값)
powershell -ExecutionPolicy Bypass -File .\tools\figma\export-figma.ps1 -Images -OnlyIds "12:34","12:56"

# 배율 바꾸기 (기본 2배)
powershell -ExecutionPolicy Bypass -File .\tools\figma\export-figma.ps1 -Images -Scale 1
```

### 다른 Figma 파일일 때

주소 `https://www.figma.com/design/<여기>/...`의 `<여기>` 부분을 넣습니다.

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\figma\export-figma.ps1 -FileKey "파일키"
```

## 3. 끝나고

1. `tools\figma\figma-export.zip`을 대화창에 올립니다.
2. Figma **Settings → Security**에서 방금 만든 토큰을 **Revoke**(삭제)합니다.
3. `out` 폴더와 zip은 `.gitignore`에 들어 있어서 커밋되지 않습니다.

## 문제 해결

| 메시지 | 원인과 해결 |
|---|---|
| `FIGMA_TOKEN 환경 변수가 없습니다` | 같은 PowerShell 창에서 `$env:FIGMA_TOKEN = "..."`를 먼저 실행 |
| `권한이 없습니다 (403)` | 토큰 복사 실수, 또는 파일을 볼 수 없는 계정의 토큰 |
| `파일을 찾을 수 없습니다 (404)` | `-FileKey` 값 확인 |
| `호출 한도를 넘었습니다 (429)` | 한도 초과. 이미 받은 `out` 폴더가 있으면 그대로 zip을 올리거나, 아래 "API 없이" 방법 사용 |
| 한글이 깨져 보임 | 결과 파일은 정상입니다. 콘솔 표시만의 문제라 무시해도 됩니다 |
| `스크립트를 실행할 수 없습니다` | 명령에 `-ExecutionPolicy Bypass`가 들어 있는지 확인 |

## API 없이 받는 방법 (한도 초과 시)

Figma 앱이나 웹에서 직접 내보냅니다.

- **로고**: 로고 프레임 선택 → 오른쪽 **Export** → `PNG 4x`와 `SVG` 두 개 추가 → Export
- **색 값**: 디자인 시스템 프레임에서 색 견본을 하나씩 선택 → 오른쪽 **Fill**의 `#RRGGBB` 값을 표로 적기
  (또는 Dev Mode가 열리면 색 견본 선택 → 오른쪽 패널의 값 복사)
- **변수(Variables)를 쓰는 파일**이면: 오른쪽 패널 **Local variables** 열기 → 이름과 값을 캡처
