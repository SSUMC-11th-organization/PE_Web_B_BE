# 8주차 필수미션: 즐겨찾기와 영화 기록

이번 주는 로그인한 사용자의 즐겨찾기를 관리하고, 평점과 즐겨찾기 저장을 한 묶음으로 처리하는 기능을 만듭니다. 두 저장을 묶는 `@Transactional`이 핵심입니다. 하나가 실패하면 이미 저장한 것도 취소합니다.

로컬 구현과 자동 테스트는 도우미가 진행하고, 실제 MySQL 서버의 Swagger 요청·캡처·Notion 제출은 사용자가 진행합니다. 이슈/PR 생성, push 및 GitHub 인증 정보 사용은 하지 않습니다. 워크북은 Postman 캡처를 명시하므로 Swagger 캡처 인정 여부는 스터디 기준을 확인하세요.

## 1. 서버 켜기

1. MySQL Workbench에서 평소 쓰는 로컬 DB 연결을 엽니다.
2. SCHEMAS에 `umc_movie`가 있는지 확인합니다.
3. 아래 SQL로 favorite 테이블이 있는지 확인합니다. 기존 5주차 스키마에 포함된 테이블입니다.

```sql
USE umc_movie;
SHOW CREATE TABLE favorite;
```

없다면 `movie/sql/01-schema.sql`의 `CREATE TABLE IF NOT EXISTS favorite ...` 부분을 실행합니다. DB를 초기화하거나 기존 데이터를 지우지 마세요. favorite에는 member_id 외래 키와 (member_id, movie_id) 복합 UNIQUE 제약이 있어야 합니다.

기존 서버 터미널이 있으면 Ctrl+C로 종료합니다. 새 PowerShell을 열어 아래를 실행하세요. 지난주에 썼던 8081을 그대로 사용합니다.

```powershell
cd C:\dev\UMC\PE_Web_B_BE\movie
$env:DB_USER = 'root'
$dbSecret = Read-Host 'MySQL 비밀번호' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $dbSecret).Password
$jwtBytes = New-Object byte[] 48
$jwtRng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtRng.GetBytes($jwtBytes)
$jwtRng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$env:JWT_ACCESS_EXPIRATION = '3600'
$env:SERVER_PORT = '8081'
.\gradlew.bat bootRun
```

`Started MovieApplication`이 나오면 성공입니다. 이 터미널은 닫지 말고 [Swagger](http://localhost:8081/swagger-ui/index.html)를 여세요. 8081도 사용 중이면 기존 서버를 확인하거나 다른 포트를 사용해야 합니다. 비밀키를 새로 생성했으므로 지난주 토큰을 재사용하지 말고 다시 로그인하세요.

## 2. 실습 계정 만들고 로그인하기

이전 평점·즐겨찾기와 충돌하지 않도록 새 실습 계정을 쓰는 것을 권합니다. Swagger는 API 펼치기 → Try it out → 입력 → Execute 순서입니다. 결과는 `Server response`의 Code와 Response body에서 확인하세요.

`POST /members`:

```json
{
  "email": "week8-a@umc.com",
  "password": "Umc!1234",
  "nickname": "팔주차A"
}
```

201이 나오면 가입 성공입니다. 같은 계정을 이미 만들었다면 중복 가입할 필요 없이 로그인하세요. 이 계정으로 이미 실습했다면 즐겨찾기와 평점이 남아 있을 수 있으니 다른 이메일·닉네임으로 가입하거나 본인 실습 데이터를 확인하세요.

`POST /auth/login`:

```json
{
  "email": "week8-a@umc.com",
  "password": "Umc!1234"
}
```

200 응답의 `result.accessToken` 전체를 복사합니다. 위쪽 Authorize → 토큰 값만 붙여넣기 → Authorize → Close를 누릅니다. `Bearer `는 직접 붙이지 않습니다.

## 3. 즐겨찾기 실습과 캡처

### 먼저 등록 전 상태 확인

`GET /favorites/550` 실행 → 200, `result.isFavorite: false` 확인.

### 캡처 1. 즐겨찾기 등록 성공 — POST /favorites (201)

```json
{
  "movieId": 550
}
```

201, `COMMON201`, result의 favoriteId/movieId/createdAt을 확인하세요. Body에 memberId가 없는 것이 보여야 합니다. 설명: “토큰으로 로그인한 사용자를 식별하여 영화를 즐겨찾기에 등록했다.”

### 캡처 2. 동일 영화 중복 등록 차단 — POST /favorites (409)

같은 요청을 다시 Execute → 409, `FAVORITE409_1` 확인. 이 응답도 필수 제출물입니다. 설명: “같은 회원이 같은 영화를 두 번 등록하면 FAVORITE409_1로 차단된다.”

### 캡처 3. 내 즐겨찾기 목록 조회 — GET /favorites (200)

200, `result` 배열에 movieId 550이 있는지 확인합니다. 목록은 최신 등록 순서입니다. 설명: “로그인한 사용자의 즐겨찾기 목록을 조회했다.”

### 캡처 4. 영화의 즐겨찾기 여부 조회 — GET /favorites/550 (200)

200, `result.isFavorite: true` 확인. 설명: “등록한 영화의 즐겨찾기 상태가 true로 반환되는 것을 확인했다.”

### 캡처 5. 즐겨찾기 해제 — DELETE /favorites/550 (200)

200, `COMMON200`, `result: null` 확인. 경로에 넣는 값은 **favoriteId가 아니라 movieId(550)**입니다. 설명: “본인의 즐겨찾기를 해제하고 공통 성공 응답을 확인했다.”

이어서 `GET /favorites/550` → false, `GET /favorites` → 빈 배열 `[]`을 확인하세요. 빈 목록과 false는 정상 상태라 200입니다. 이 두 장은 과정 기록에 추가하면 좋습니다.

## 4. 캡처 6. 평점과 즐겨찾기 함께 저장 — POST /movie-records (201)

아직 평점도 즐겨찾기도 없는 영화 238을 사용합니다.

```json
{
  "movieId": 238,
  "score": 5,
  "comment": "평점과 즐겨찾기를 함께 저장했습니다.",
  "addToFavorites": true
}
```

201, `COMMON201`과 함께 **result.rating과 result.favorite가 모두 있는 화면**을 캡처하세요. 설명: “평점과 즐겨찾기를 하나의 요청으로 함께 저장했다.”

같은 요청을 다시 보내면 409, `RATING409_1`이어야 합니다. `GET /favorites`와 `GET /members/{memberId}/ratings`로 중복 행이 늘지 않는지도 확인할 수 있습니다. 이 공개 평점 조회의 memberId는 회원가입 응답의 번호를 사용합니다. 새로 만든 8주차 API에는 memberId를 입력하지 않습니다.

평점만 저장하는 동작도 확인하려면 다른 영화로 아래 요청을 보냅니다.

```json
{
  "movieId": 155,
  "score": 4,
  "comment": "평점만 남겼습니다.",
  "addToFavorites": false
}
```

201, result.rating은 객체, result.favorite는 null이 나와야 합니다.

## 5. 캡처 7. 트랜잭션 핵심 코드

`src/main/java/com/umc/movie/service/MovieRecordService.java`를 열어 `@Transactional`과 ratingRepository.save, favoriteRepository.save가 같은 메서드에 있는 것이 보이도록 캡처합니다. 길면 나눠 찍어도 됩니다.

바로 아래 붙일 한 문장:

> 평점과 즐겨찾기 중 하나만 저장되는 것을 막기 위해 하나의 트랜잭션으로 묶어, 모두 성공하면 저장하고 중간에 실패하면 먼저 저장한 내용도 취소하도록 했다.

같은 요청의 중복 409는 저장 **전** 사전 검사로 거절되므로 그것만으로 저장 **후** 롤백을 입증하지는 않습니다. 자동 테스트에서는 평점 INSERT 뒤 실패를 발생시키고 요청 종료 후 평점이 남지 않는지, DB UNIQUE 제약에 걸려도 앞선 평점이 취소되는지 별도로 확인합니다. 실습을 위해 운영 코드에 강제 실패 API를 추가할 필요는 없습니다.

## 6. 추가 실습 체크리스트

- Authorize에서 Logout 후 `POST /favorites` → 401, `AUTH401_2`. 이후 다시 토큰 등록.
- `POST /favorites` Body의 movieId를 0으로 전송 → 400.
- 해제한 550을 다시 `DELETE /favorites/550` → 404, `FAVORITE404_1`.
- 회원 B의 토큰으로 조회하면 A의 즐겨찾기가 나오지 않는지 확인.

## 구현과 자동 검증 범위

2026-11-17 검증: `gradlew.bat test bootJar` 성공. 기존 26개와 신규 11개를 합쳐 자동 테스트 37개 통과. H2 MySQL 호환 모드/MockMvc 결과이며, 실제 MySQL 서버의 Swagger 실행 및 제출 캡처는 아직 남아 있습니다.

- Favorite 엔티티는 BaseTimeEntity를 상속하고 Member와 movieId를 저장합니다.
- (member_id, movie_id) 복합 UNIQUE 제약과 사전 중복 검사를 함께 사용합니다. 실제 동시 요청의 DB 제약 실패는 기존 전역 처리기의 COMMON4091 응답으로 처리하며, 순차 중복은 FAVORITE409_1입니다.
- FavoriteService에서 로그인한 회원 ID를 조건으로 목록·상태·삭제를 처리합니다.
- MovieRecordService.create의 @Transactional이 두 저장을 묶습니다.
- Swagger Bearer 인증 표시를 추가했습니다. 기존 인증 인터셉터는 변경하지 않았습니다.
- 영화 ID 양수, 별점 1~5, addToFavorites 필수, 기존 후기 규칙과 동일한 최대 500자를 검사합니다.
- 테스트 데이터 정리 시 favorite를 먼저 지워 새 외래 키와 충돌하지 않게 했습니다. 실제 MySQL 데이터에는 이 테스트 SQL을 실행하지 않습니다.

워크북의 “각 서비스를 부르면 각각 별도 트랜잭션일 수 있다”는 설명은 **바깥 트랜잭션이 없는 경우**로 이해하면 됩니다. Spring의 기본 REQUIRED는 바깥 트랜잭션이 있으면 거기에 참여합니다. 이번에는 워크북대로 전용 Service에서 하나의 경계를 명시합니다. [Spring 공식 트랜잭션 전파 설명](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html)

기본 설정은 RuntimeException 또는 Error가 메서드 밖으로 전파되면 롤백합니다. ApiException은 RuntimeException입니다. 예외를 잡고 정상 반환하면 의도한 롤백이 되지 않을 수 있습니다. [Spring 공식 롤백 설명](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html)

## Notion 최소 기록 형식

1. “Favorite 엔티티와 API를 추가하고, 영화 기록 저장을 @Transactional로 묶었다.”라는 과정 요약
2. 위 캡처 1~6과 각각의 설명
3. 캡처 7과 트랜잭션으로 묶은 이유 한 문장
4. 핵심 코드 링크(사용자가 GitHub에 올린 후 추가)
5. 실제로 막힌 점과 해결 과정이 있다면 작성. 없던 경험을 만들지 않기

핵심 코드: entity/Favorite.java, service/FavoriteService.java, service/MovieRecordService.java, controller/FavoriteController.java, controller/MovieRecordController.java. 이후 커밋·push·이슈·PR은 사용자가 직접 진행합니다.
