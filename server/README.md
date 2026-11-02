# server

Kotlin + Spring Boot 4 API 서버입니다.

## 실행

JDK 21이 필요합니다. (IntelliJ IDEA에서 `server/` 폴더를 열어도 됩니다.)

```bash
cd server
./gradlew bootRun          # http://localhost:8080 에서 실행
./gradlew test             # 테스트
./gradlew build            # 빌드 + 테스트
```

Windows에서는 `./gradlew` 대신 `gradlew.bat`을 사용합니다.

## 확인

```bash
curl http://localhost:8080/api/v1/ping
curl http://localhost:8080/actuator/health
```
