# 7주차 필수미션: 로그인한 사용자 구분하기

첨부 워크북의 Spring Boot 실습 1~6을 기존 `movie/`에 적용했습니다. NestJS 구현과 선택 미션의 `@LoginMember`는 이번 필수 범위에 포함하지 않았습니다.

## 완료한 구현

| 기능 | 코드에서 확인할 곳 | 동작 |
| --- | --- | --- |
| 로그인 | AuthController → AuthService | 이메일 조회 → bcrypt matches → JWT 발급 |
| 토큰 검증 | AuthInterceptor → JwtUtil | Authorization에서 토큰 추출, 서명·만료·sub 검증 |
| 내 정보 조회/수정 | MemberController → MemberService | 토큰의 memberId 사용, 비밀번호는 응답에서 제외 |
| 평점 쓰기 | RatingController → RatingService | URL의 memberId 제거, 작성자와 요청자 비교 |
| 실패 응답 | ApiException → GlobalExceptionHandler | HTTP 상태와 도메인 code를 공통 응답 형식에 담음 |

인증 설정은 `AuthConfig`에 따로 등록했습니다. 이전 주차 CORS 비교 실습을 위해 `CORS_ENABLED=false`로 실행해도 인증이 꺼지지 않습니다. 공개 API는 메서드까지 확인하며 Swagger 문서는 로컬 실습을 위해 공개합니다.

## 1. 서버 실행

Java 21과 기존 6주차 MySQL DB가 필요합니다. 기존 테이블 구조는 변경하지 않습니다. MySQL이 아직 준비되지 않았다면 `movie/README.md`와 `sql/01-schema.sql`을 참고하세요. 초기 SQL의 dummy 비밀번호 회원은 로그인용 계정이 아니므로 아래 Postman 회원가입으로 새 계정을 만드세요.

PowerShell에서:

```powershell
cd C:\dev\UMC\PE_Web_B_BE\movie
$env:DB_USER = 'root'
$dbSecret = Read-Host 'MySQL 비밀번호' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $dbSecret).Password
# 실습 서버에 사용할 무작위 비밀키를 생성합니다. 재시작 시 바꾸면 기존 토큰은 무효입니다.
$jwtBytes = New-Object byte[] 48
[System.Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$env:JWT_ACCESS_EXPIRATION = '3600'
.\gradlew.bat bootRun
```

위 키 생성은 PowerShell 7 기준입니다. Windows PowerShell 5.1에서는 `Fill` 대신 다음을 사용하세요.

```powershell
$jwtRng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtRng.GetBytes($jwtBytes)
$jwtRng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
```

IntelliJ 실행 설정을 사용한다면 `DB_PASSWORD`, `JWT_SECRET`, `JWT_ACCESS_EXPIRATION`을 환경 변수에 넣습니다. `.env`는 자동으로 읽지 않습니다. 비밀키는 UTF-8 기준 최소 32바이트여야 하며 Git에 올리지 않습니다. `movie/.gitignore`는 `.env`와 `.env.*`를 제외합니다. DB 비밀번호를 채팅에 공유할 필요는 없습니다.

## 2. Postman 필수 9단계

`postman/week7.postman_collection.json`을 Import하고 `필수 9단계 (위에서 아래로)` 폴더를 순서대로 실행하세요. 첫 요청이 매번 새로운 이메일/닉네임을 생성하고 로그인 응답의 tokenA/tokenB, 등록 응답의 ratingId를 자동 저장합니다. 첫 요청부터 다시 실행하면 계정 두 개가 새로 생깁니다. 테스트용 DB를 사용하세요.

| 단계 | 요청 | 예상 상태 / code |
| --- | --- | --- |
| 1 | A, B 회원가입 | 각각 201 / COMMON201 |
| 2 | A, B 로그인 | 각각 200 / COMMON200 |
| 3 | A로 평점 등록 | 201 / COMMON201 |
| 4 | A로 동일 영화 재등록 | 409 / RATING409_1 |
| 5 | A로 본인 평점 수정 | 200 / COMMON200 |
| 6 | B로 A 평점 수정 | 403 / RATING403_1 |
| 7 | 토큰 없이 평점 수정 | 401 / AUTH401_2 |
| 8 | 없는 평점 삭제 | 404 / RATING404_1 |
| 9 | A로 본인 평점 삭제 | 200 / COMMON200, result=null |

8단계는 기존 데이터와 충돌을 피하기 위해 워크북의 99999 대신 Long 최댓값을 사용했습니다. 반드시 99999를 써야 하는 제출 기준이면 URL을 바꾸고 해당 ID가 없는지 확인하세요.

추가 폴더도 실행하면 토큰 없는 내 정보, 정상 내 정보, 동일 닉네임 허용, 다른 회원 닉네임 중복 거부, 동일한 로그인 실패 응답, payload 변조 실패를 확인할 수 있습니다. 토큰 만료 전 실행하세요.

## 3. 필수 제출 캡처 4장

요청 Method/URL, 응답 HTTP 상태, 응답 JSON을 함께 보이게 캡처하세요. 실제 Postman 응답 캡처는 아직 남은 작업입니다.

1. **로그인 성공**: `2-A. 로그인`의 응답. `result.accessToken`이 보여야 합니다.
2. **인증 실패**: 추가 폴더의 `토큰 없이 내 정보`. `GET /members/me`, 401, `AUTH401_2`가 보여야 합니다. 평점 수정의 401 캡처로 대체하지 마세요.
3. **인가 실패**: `6. B가 A 평점 수정`. 403, `RATING403_1`이 보여야 합니다. 9단계 삭제 전에 캡처하세요.
4. **본인 등록**: `3. A 평점 등록`. Body 탭에서 movieId/score/comment만 있고 memberId가 없는 것과 201 성공 응답이 보여야 합니다.

Authorization 탭에서 변수 토큰이 적용되는 것을 확인하세요. 응답의 토큰을 공개 공유한다면 실습 계정용 단기 토큰만 쓰고 만료 후 제출하는 편이 좋습니다. 실제 키를 캡처할 필요는 없습니다.

## 4. 이해하고 직접 확인할 내용

- **인증**은 요청자가 누구인지 확인하는 것입니다. 로그인 때뿐 아니라 보호된 요청마다 JWT를 검증합니다. 워크북 표의 ‘로그인할 때 한 번’은 이 과정을 생략한 요약입니다.
- **인가**는 확인된 요청자가 해당 작업을 해도 되는지 확인합니다. 유효한 B 토큰이어도 A 평점은 바꿀 수 없습니다.
- **bcrypt**는 비밀번호를 복호화하지 않습니다. 저장된 해시 안의 salt와 비용을 이용해 `matches`로 입력값을 비교합니다. 같은 비밀번호도 가입할 때마다 해시가 달라질 수 있습니다.
- **JWT**는 header.payload.signature입니다. payload는 Base64URL 인코딩이라 읽을 수 있고, 서명 검증이 조작을 막습니다. sub=회원 ID, email=참고 정보, iat=발급 시각, exp=만료 시각입니다. 비밀번호는 넣지 않습니다.
- **Bearer**는 토큰 소지자를 인증하는 방식입니다. `Authorization: Bearer {token}`을 보냅니다. Postman Bearer Token 입력 칸에는 토큰 값만 넣습니다.
- **Interceptor**는 컨트롤러 전에 인증을 처리하고 memberId를 요청 속성에 넣습니다. 서비스는 DB의 실제 작성자와 비교해 인가를 처리합니다.
- **401**은 인증 실패, **403**은 인증된 요청자의 권한 부족입니다. 존재하지 않는 평점은 404로 구분합니다.
- 로그인 실패는 이메일 없음/틀린 비밀번호 모두 `AUTH401_1`과 동일한 메시지입니다. 실패 응답으로 이메일 존재를 구분하지 못하게 합니다. 다만 이전 주차의 공개 이메일 중복 확인 API도 존재하므로 이 조치 하나가 가입 여부 노출을 모두 차단하는 것은 아닙니다.

jwt.io에는 실습 계정의 토큰만 붙여넣고 sub/email/iat/exp를 확인하세요. 비밀키 없이도 payload를 볼 수 있다는 것이 핵심입니다. 비밀키를 사이트에 입력할 필요는 없습니다.

워크북은 `clockSkewSeconds(60)`을 쓰면서 선택 미션에서는 60초 뒤 즉시 만료를 기대합니다. 유예가 있으면 만료가 늦게 관찰될 수 있어 이 구현은 유예를 두지 않습니다. [JJWT 공식 문서](https://github.com/jwtk/jjwt#accounting-for-clock-skew)는 이 설정이 exp/nbf 시간 검사 허용 오차라고 설명합니다. 선택 만료 실습은 환경 변수를 60으로 바꾸고 재시작한 뒤 **새로 로그인한 토큰**으로 진행하세요.

## 5. 검증과 남은 작업

2026-11-10 검증 결과: `test bootJar` 성공, 자동 테스트 26개 통과(실패 0). Postman 컬렉션의 스크립트 20개는 JavaScript 구문 검사를 통과했습니다. 실제 Postman 앱 실행 결과는 아직 확인하지 않았습니다.

```powershell
cd C:\dev\UMC\PE_Web_B_BE\movie
.\gradlew.bat test bootJar
```

자동 테스트는 H2의 MySQL 호환 모드와 MockMvc를 사용합니다. 실제 MySQL 연결과 Postman UI에서의 9단계 실행/캡처를 대신하지 않습니다. 테스트는 회원가입·로그인·평점 등록/중복/수정/남의 수정 및 삭제 거부/무토큰/없는 평점/본인 삭제, 토큰 변조·만료, 내 정보 수정 규칙, 기존 조회 및 CORS를 검증합니다.

직접 할 일은 서버 실행에 필요한 본인 MySQL 비밀번호 입력, Postman 실행 및 캡처 4장, jwt.io payload 관찰, 본인 설명/회고 작성입니다. 워크북에 요구된 스터디 인증 사진과 Notion 공유 및 UMC 제출도 본인이 진행해야 합니다. 과정 기록은 `week7-record.md`를 활용하세요.
