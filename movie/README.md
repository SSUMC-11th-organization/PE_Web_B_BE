# 영화 평점 스터디 프로젝트

**7주차 필수미션**: [실행·Postman 9단계·캡처 가이드](docs/week7-mission.md), [미션 기록 초안](docs/week7-record.md), [Postman 컬렉션](postman/week7.postman_collection.json). 현재 서버는 `DB_PASSWORD`와 `JWT_SECRET` 환경 변수가 필요합니다. 평점 쓰기 URL은 `/ratings`로 변경되었으며 아래 이전 주차 명세는 학습 이력입니다.

6주차 필수 미션의 API 명세·Swagger 실행 순서·캡처 가이드는 [week6-mission.md](docs/week6-mission.md)를 참고하세요. 5주차 프로젝트에서 회원가입과 평점 쓰기 기능을 확장했습니다.

Java 21 / Spring Boot 3.5.0 / MySQL. 기존 `study/`는 도서 대여 프로젝트로 보존하고 영화 프로젝트를 새로 생성했습니다. 저장소 README의 기존 `study/` 규칙과 이번 워크북의 새 프로젝트 요구를 함께 고려한 배치입니다.

## 1. MySQL 준비 (본인 PC에서 한 번)

MySQL Workbench로 로컬 DB에 접속하여 `sql/01-schema.sql`, `sql/02-seed.sql`을 순서대로 실행합니다. 새 DB `umc_movie`만 사용하고 기존 도서 DB는 수정하지 않습니다. 스키마 스크립트는 기존 테이블 구조를 변경하지 않으므로 같은 이름의 이전 테이블이 있으면 구조를 먼저 확인하세요.

마지막 SELECT의 회원 ID를 확인하세요. 빈 DB에서는 수현=1, 민서=2이며 기존 데이터가 있으면 달라질 수 있습니다. 비밀번호 더미는 로그인용 bcrypt 해시가 아닙니다. Favorite은 이번 주차에 테이블만 만들고 엔티티/API는 8주차에 추가합니다.

## 2. 백엔드 실행

PowerShell에서 저장소 루트 기준:

```powershell
cd C:\dev\UMC\PE_Web_B_BE\movie
$env:DB_USER = 'root'
$secret = Read-Host '로컬 MySQL 비밀번호' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secret).Password
.\gradlew.bat bootRun
```

비밀번호는 채팅에 보내지 마세요. IntelliJ에서는 `movie/`를 Gradle 프로젝트로 열고 실행 설정의 환경 변수에 `DB_PASSWORD`를 지정하면 됩니다. 기존 프로젝트의 `DB_PW`와 이름이 다릅니다. `.env` 파일은 자동으로 읽지 않습니다. 서버 주소는 `http://localhost:8080`이며 기존 `study/` 서버가 실행 중이면 종료해주세요.

## 3. 기존 UMCine 화면 실행 (선택한 필수 미션 화면)

기존 5주차 프론트의 `src/components/member-api-panel.tsx`를 추가하고 `src/routes/__root.tsx`에 연결했습니다. 화면 상단에서 닉네임/이메일 중복 확인과 회원별 평점 조회를 사용할 수 있습니다. 각 결과의 ‘응답 JSON 보기’를 열어 요청 URL, HTTP 상태와 응답을 함께 캡처하세요.

다른 PowerShell에서:

```powershell
cd C:\dev\UMC\PE_Web_B\mins\Week5\Week5_Mission\umcine
npm run dev -- --port 5173 --strictPort
```

`http://localhost:5173`에 접속하세요. 프론트의 백엔드 주소는 기본 `http://localhost:8080`이며 필요하면 Vite 환경 변수 `VITE_BACKEND_URL`로 변경하고 개발 서버를 재시작합니다. 기존 TMDB 요청은 그대로 유지합니다.

## 3-1. 별도 CORS 실습 화면 (선택)

다른 PowerShell에서:

```powershell
cd C:\dev\UMC\PE_Web_B_BE\movie
node .\mission-web\serve.cjs
```

브라우저에서 `http://localhost:5173`에 접속합니다. 닉네임, 이메일, 평점 버튼을 **모두** 눌러 세 응답을 확인합니다. 실습 화면의 파일을 더블클릭하면 출처가 달라지므로 반드시 HTTP 주소로 여세요.

이 화면은 세 API를 사용하는 독립 실습 웹입니다. 필수 인증은 위의 기존 UMCine에서 진행하세요. 기존 프론트와 동시에 실행하면 5173 포트 충돌이 발생합니다.

## 4. CORS 전/후 재현

백엔드 터미널에서 Ctrl+C로 종료한 뒤:

```powershell
$env:CORS_ENABLED = 'false'
.\gradlew.bat bootRun
```

화면에서 F12 → Console/Network를 열고 버튼을 눌러 실패를 캡처합니다. API가 실제로 200을 반환하는데 브라우저에서 응답을 읽지 못하는지 확인하세요. 404/서버 종료를 CORS 실패 증거로 혼동하지 마세요.

다시 Ctrl+C 후:

```powershell
$env:CORS_ENABLED = 'true'
.\gradlew.bat bootRun
```

같은 화면에서 다시 호출해 200과 JSON을 캡처합니다. 기본 정책은 `/members/**`, GET, `http://localhost:5173`입니다. `127.0.0.1:5173`과 `localhost:5173`은 다른 출처입니다.

선택 CORS 비교 실습은 `CORS_ALLOWED_ORIGINS='*'`로 재시작하고 다른 출처(예: 화면 서버 PORT=5174)에서도 요청 후, 다시 `http://localhost:5173`으로 제한해 재시작하여 비교합니다. 끝나면 기본 출처와 PORT=5173으로 복구합니다.

## 5. Postman과 제출

Swagger UI도 제공합니다. 서버를 재시작한 뒤 `http://localhost:8080/swagger-ui/index.html`에서 세 API를 선택하고 Try it out → 값 입력 → Execute로 요청합니다. Request URL, Server response의 Code 200, Response body가 함께 보이게 캡처하세요. 워크북은 Postman을 명시하므로 Swagger 캡처의 대체 인정 여부는 스터디 운영 기준을 확인해야 합니다.

`postman/week5.postman_collection.json`을 Import하세요. collection 변수 `baseUrl`과 `memberId`를 필요에 맞게 수정합니다. 요청 URL, HTTP 상태, JSON이 한 화면에 보이게 캡처합니다. 요청마다 자동 검증 스크립트가 포함되어 있습니다.

필수 제출 준비물:

- Entity, DTO, Controller/Service 핵심 코드 (코드 링크/캡처)
- 중복 닉네임 false / 새 닉네임 true, 중복 이메일 false / 새 이메일 true
- 회원 평점 최신순, 없는 회원 404
- CORS 설정 전 실패와 설정 후 성공
- 실제 화면의 3개 API 정상 호출 결과
- 과정/트러블슈팅/본인 회고를 채운 Notion 페이지

`docs/mission-record.md`에 붙여넣기용 기록을 작성했습니다. 캡처와 실제 경험은 본인이 채워야 합니다. 제출 완료, 스터디 인증 사진, 본인 회고를 임의로 작성하지 않았습니다. Notion 페이지 공유 링크를 UMC 사이트에 직접 제출하세요. 이 작업에서는 GitHub push/PR 생성이나 Notion 게시를 수행하지 않습니다.

## 검증

```powershell
.\gradlew.bat test bootJar
```

자동 테스트는 H2의 MySQL 호환 모드를 사용하며 조회/정렬/빈 목록/404/입력 400/허용·차단 CORS/설정 비활성화를 검증합니다. MySQL 스키마 검증과 실제 브라우저/Postman 캡처를 대체하지 않습니다.

## API 명세

| API | Method / URL | 입력 | 성공 응답 |
| --- | --- | --- | --- |
| 닉네임 확인 | GET /members/nickname/{nickname} | Path string, 공백만 불가, 2~12자 | 200 `{ "available": true }` |
| 이메일 확인 | GET /members/email/{email} | Path string, 이메일 형식, 최대 254자 | 200 `{ "available": true }` |
| 회원 평점 | GET /members/{memberId}/ratings | Path 양의 Long | 200 `RatingResponse[]`, ratingId 내림차순 |

평점 DTO: `ratingId`, `movieId`, `score`, `comment`(null 가능), `createdAt`, `updatedAt`. 회원은 있으나 평점이 없으면 `[]`, 회원이 없으면 404, 형식 오류는 400. `available=true`는 미사용, false는 중복입니다. 닉네임/이메일은 프론트에서 `encodeURIComponent`로 인코딩합니다. 비밀번호/회원 엔티티는 응답하지 않습니다. 페이지네이션 선택 미션은 적용하지 않아 기본 배열 계약을 유지합니다.
