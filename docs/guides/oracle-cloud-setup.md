# Oracle Cloud 무료 계정 만들기

서버, DB, 이미지 저장소를 **Oracle Cloud Infrastructure(OCI) Always Free**에 올립니다.
이 문서는 계정 만들기까지 다룹니다. VM 생성과 배포는 Phase 1에서 이어서 진행합니다.

> 2026-11 기준으로 정리한 내용입니다. Oracle이 무료 한도를 예고 없이 바꾼 적이 있으니, 가입 전에 [공식 Always Free 문서](https://docs.oracle.com/en-us/iaas/Content/FreeTier/resourceref.htm)를 한 번 확인하세요.

## 우리가 쓸 무료 자원

| 자원 | Always Free 한도 | 우리 계획 |
|---|---|---|
| Arm VM (Ampere A1) | 합계 **2 OCPU / 12GB 메모리** (2026년에 4 OCPU / 24GB에서 절반으로 줄었음) | 1대로 서버 + PostgreSQL + Redis 실행 |
| AMD VM (E2.1.Micro) | 1/8 OCPU, 1GB 메모리 2대 | 사용 안 함 (메모리 부족) |
| 블록 스토리지 | 합계 200GB | 부트 볼륨 + DB 데이터 |
| Object Storage | Standard 10GB + Infrequent Access 10GB | 이미지, 학생증(비공개 버킷), DB 백업 |

인터넷에 "4 OCPU / 24GB 무료"라고 적힌 글이 아직 많은데, 지금은 맞지 않습니다.

## 준비물

- 이메일 주소
- **해외 결제가 되는 본인 명의 신용카드 또는 체크카드** (VISA / Mastercard). 본인 확인 용도이고, Always Free 범위 안에서만 쓰면 요금이 나가지 않습니다.
- 휴대폰 (문자 인증)
- 집 주소 영문 표기 (네이버에서 "영문주소 변환"으로 검색해 바꿀 수 있습니다)

## 가입 순서

1. **가입 페이지 열기**
   [oracle.com/kr/cloud/free](https://www.oracle.com/kr/cloud/free/)에서 "무료로 시작하기"를 누릅니다.

2. **기본 정보 입력**
   국가는 대한민국을 고르고, 이름은 **카드에 적힌 영문 이름과 똑같이** 적습니다. 이메일 인증 메일이 오면 링크를 누릅니다.

3. **비밀번호와 Cloud Account Name**
   Cloud Account Name(테넌시 이름)은 로그인할 때마다 필요하니 따로 적어 둡니다. 예: `caubuddy`

4. **홈 리전 선택 (나중에 바꿀 수 없음)**
   Always Free 자원은 홈 리전에서만 만들 수 있습니다. 한국 리전인 `South Korea Central (Seoul)`이나 `South Korea North (Chuncheon)` 중 하나를 고르세요. 사용자가 국내에 있으니 둘 중 어느 쪽이든 괜찮습니다.

5. **주소와 전화번호**
   영문 주소를 입력하고 문자로 전화번호를 인증합니다.

6. **카드 인증**
   카드 정보를 입력합니다. 확인용 소액 승인이 잡혔다가 취소될 수 있습니다.

7. **약관 동의 후 계정 생성**
   "계정 생성 중" 화면이 뜨고, 보통 몇 분에서 수십 분 뒤에 완료 메일이 옵니다. 메일이 오면 [cloud.oracle.com](https://cloud.oracle.com)에서 Cloud Account Name → 이메일 → 비밀번호 순으로 로그인합니다.

## 가입이 안 될 때

"등록을 완료할 수 없습니다" 같은 오류가 자주 보고됩니다. 아래를 확인하고 다시 해 보세요.

- 카드 명의, 가입 이름, 주소가 서로 맞는지
- VPN을 끄고, 다른 브라우저나 시크릿 창으로 시도
- 다른 카드(VISA ↔ Mastercard)로 시도

그래도 계속 실패하면 알려 주세요. 다른 무료 호스팅 방법을 찾아보겠습니다.

## 가입 직후 꼭 할 일

1. **예산 알림 걸기**
   콘솔 메뉴에서 Billing & Cost Management → Budgets로 들어가 월 예산 US$1, 알림 100%로 만듭니다. 혹시라도 유료 자원이 생기면 메일로 알 수 있습니다.

2. **유료 업그레이드(Pay As You Go) 버튼은 누르지 않기**
   무료 계정으로 충분합니다.

3. **처음부터 무료 한도 안에서만 만들기**
   가입하면 30일 체험 크레딧이 같이 주어집니다. 체험이 끝날 때 Arm VM 합계가 2 OCPU / 12GB를 넘으면, 넘은 만큼만이 아니라 **Arm VM 전체가 비활성화**되고 30일 뒤 삭제됩니다. VM은 항상 2 OCPU / 12GB 이하로 만드세요.

4. **계정을 오래 방치하지 않기**
   30일 넘게 아무 활동이 없는 계정은 버려진 것으로 보고 정지되거나 정리될 수 있습니다. 개발 기간에는 서버가 계속 돌고 있으니 문제가 없습니다.

## 다음 단계 (Phase 1에서 같이 진행)

- [ ] VM 만들기: Shape `VM.Standard.A1.Flex`, **2 OCPU / 12GB**, Ubuntu (aarch64), 부트 볼륨 50GB
  - "Out of capacity" 오류가 나면 무료 Arm 자원 재고가 없다는 뜻입니다. 시간을 두고 다시 시도하거나 다른 가용 도메인(AD)을 고릅니다.
- [ ] SSH 키 내려받아 안전한 곳에 보관 (잃어버리면 VM에 접속할 수 없음)
- [ ] 네트워크 보안 목록에서 80, 443 포트 열기
- [ ] Docker 설치, 서버 이미지는 Arm64(`linux/arm64`)용으로 빌드
- [ ] Object Storage 버킷 2개 만들기: 공개 이미지용, 학생증용(비공개)
