# infra

## 로컬 개발 환경

필요한 것: [Docker Desktop](https://www.docker.com/products/docker-desktop/)

```bash
cd infra
cp .env.example .env      # 처음 한 번만
docker compose up -d      # PostgreSQL, Redis 실행
docker compose ps         # 상태 확인 (healthy가 보이면 준비 완료)
docker compose down       # 종료 (데이터는 볼륨에 남음)
docker compose down -v    # 종료 + 데이터 삭제
```

| 서비스 | 주소 | 용도 |
|---|---|---|
| PostgreSQL 17 | `localhost:5432` | 주 데이터베이스 (Phase 1부터 사용) |
| Redis 7 | `localhost:6379` | 채팅 Pub/Sub, 캐시 (Phase 4부터 사용) |

포트는 `127.0.0.1`에만 열려 있어서 같은 와이파이의 다른 기기에서는 접속할 수 없습니다.

## 운영 환경

Azure for Students에 배포합니다.

| 로컬 | 운영 (Azure) |
|---|---|
| PostgreSQL 컨테이너 | Azure Database for PostgreSQL (관리형) |
| Redis 컨테이너 | VM 안의 Redis 컨테이너 |
| 서버 로컬 실행 | VM 안의 서버 컨테이너 + Nginx |
| 로컬 디스크 | Azure Blob Storage |

Phase 1에서 배포 스크립트와 운영용 Compose 파일을 추가합니다.
계정 만들기는 [docs/guides/azure-for-students-setup.md](../docs/guides/azure-for-students-setup.md)를 보세요.
