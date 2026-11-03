# 6주차 필수 미션: 입력한 데이터 저장하기

기존 `movie/` 프로젝트를 이어서 사용합니다. 이번 주차는 새 DB/프로젝트를 만들지 않습니다. 5주차 SQL을 다시 실행하거나 기존 데이터를 삭제할 필요가 없습니다.

## 구현 범위

- 회원가입 DTO 검증: 이메일, 닉네임 2~12자, 비밀번호 대문자·소문자·숫자·특수문자(!@#$%^&*) 포함 8자 이상.
- bcrypt 해시 저장과 비밀번호를 제외한 응답 DTO.
- 평점 등록·수정·삭제, 별점 정수 1~5, 영화 ID 양의 정수, 후기 최대 500자.
- 중복 회원·평점 409, 입력 오류 400, 소유자 불일치 403, 없는 회원/평점 404.
- 쓰기 API의 성공/실패를 `isSuccess`, `code`, `message`, `result`로 통일. DELETE 성공은 HTTP 표준과 워크북에 맞게 204이며 본문 없음.
- 워크북 예제와 동일하게 5주차 GET 성공 응답은 기존 객체/배열을 유지. 기존 UMCine 조회 화면을 깨뜨리지 않음.
- 사용자 ID는 6주차 요구대로 Path에서 받고, 로그인·JWT는 이번 범위에 포함하지 않음.

## API 명세

| 기능 | Method / URL | 입력 | 성공 |
| --- | --- | --- | --- |
| 회원가입 | POST /members | JSON email, password, nickname | 201 / result: memberId, email, nickname |
| 평점 등록 | POST /members/{memberId}/ratings | Path memberId / JSON movieId, score, comment(선택) | 201 / result: ratingId 숫자 |
| 평점 수정 | PATCH /members/{memberId}/ratings/{ratingId} | Path memberId, ratingId / JSON score, comment(선택) | 200 / result: null |
| 평점 삭제 | DELETE /members/{memberId}/ratings/{ratingId} | Path memberId, ratingId | 204 / 본문 없음 |
| 수정·삭제 결과 확인 | GET /members/{memberId}/ratings | Path memberId | 200 / 기존 평점 배열 |

PATCH에서도 score는 필수입니다. comment를 생략하거나 null로 보내면 기존 후기는 null로 지워집니다. movieId는 수정하지 않습니다. 최신순은 ratingId 내림차순입니다.

실패 응답 예시:

```json
{
  "isSuccess": false,
  "code": "COMMON4092",
  "message": "이미 사용 중인 이메일입니다.",
  "result": null
}
```

code는 요청 DTO/경로/JSON 형식 오류 COMMON4001, 서비스의 400 COMMON400, 서비스 사전 중복 COMMON4092, DB 제약 COMMON4091, 소유자 오류 COMMON403, 없는 자원 COMMON404입니다. 선택 Challenge의 이메일·닉네임 개별 code 분리는 진행하지 않습니다. DB UNIQUE 예외의 공통 응답은 필수의 안전한 중복 처리에 포함했습니다.

## 직접 실행할 것

### 1. 백엔드 재시작

실행 중인 백엔드 PowerShell에서 Ctrl+C 후, 같은 창에서 아래를 실행합니다. 같은 창을 쓰면 기존 DB_PASSWORD 환경 변수가 유지됩니다.

```powershell
cd C:\dev\UMC\PE_Web_B_BE\movie
.\gradlew.bat bootRun
```

`Started MovieApplication`이 나오면 [Swagger](http://localhost:8080/swagger-ui/index.html)를 엽니다. 새 PowerShell을 썼다면 README의 DB 환경 변수 설정을 먼저 진행하세요. 비밀번호를 채팅·노션·Git에 올리지 않습니다.

### 2. Swagger 사용법

API 클릭 → Try it out → Path/JSON 입력 → Execute. 캡처에는 Request URL, 요청 JSON(가능하면 함께), Server response의 Code와 Response body가 보여야 합니다. Example Value는 실제 응답이 아닙니다.

6주차 자료는 `Postman 또는 화면 요청/응답 캡처`를 제출물로 명시합니다. 이 가이드는 사용자의 기존 Swagger 사용 방식에 맞췄습니다. 스터디의 별도 캡처 기준이 있으면 해당 기준을 따릅니다. 실제 영화 화면 추가는 이번 필수 미션에 명시되지 않아 프론트 코드를 새로 수정하지 않았습니다.

### 3. 순서대로 실행하고 캡처

**① 회원가입 201 — 캡처**

POST /members:

```json
{"email":"week6@example.com","password":"Umc!1234","nickname":"영화팬6"}
```

응답의 `result.memberId`를 메모합니다. 아래 `<내회원ID>`는 반드시 그 값으로 바꿉니다. 응답에는 password가 없어야 합니다. 이미 가입한 값이면 다른 이메일·닉네임으로 변경합니다. 여러 번 가입해서 새 회원을 만들 필요 없이 첫 성공의 ID를 사용하세요.

**② 같은 회원가입을 다시 실행 → 409 — 캡처**

①의 JSON을 그대로 한 번 더 보냅니다. isSuccess=false, 오류 코드, 중복 이유가 나오는지 확인합니다. 필수 실패 사례 첫 번째입니다.

**③ 평점 등록 201 — 캡처**

POST /members/<내회원ID>/ratings:

```json
{"movieId":550,"score":5,"comment":"6주차에 등록한 평점입니다."}
```

응답의 `result` 숫자가 새 평점 ID입니다. 아래 `<평점ID>`에 사용합니다.

**④ 잘못된 별점 400 — 캡처**

같은 평점 등록 URL에 아래 JSON을 보냅니다. DTO 검증이 먼저 동작하여 409가 아닌 400이 나와야 합니다.

```json
{"movieId":155,"score":6,"comment":"잘못된 별점 테스트"}
```

오류 JSON의 isSuccess=false, 별점 범위 설명을 확인합니다. 필수 실패 사례 두 번째입니다. 같은 400 검증이 회원가입 실습 인증에도 필요하면 POST /members에서 이메일을 `not-email`로 바꿔 추가 확인하세요.

**⑤ 평점 수정 200 — 캡처**

PATCH /members/<내회원ID>/ratings/<평점ID>:

```json
{"score":4,"comment":"6주차에 수정한 후기입니다."}
```

이어서 GET /members/<내회원ID>/ratings를 호출해 실제 score=4, 수정 후기가 저장되었는지 확인합니다. 필요하면 조회 결과도 함께 캡처합니다.

**⑥ 다른 회원으로 수정 시도 403 — 캡처**

평점ID는 그대로 두고 Path의 memberId만 기존 시드 회원 `1`로 바꿔 같은 PATCH를 보냅니다. 내회원ID가 1이면 대신 다른 실제 회원 ID를 사용합니다. 평점 소유자와 다른 회원이면 됩니다. 실패 응답 뒤 GET으로 원래 데이터가 변하지 않았는지도 확인합니다.

**⑦ 없는 평점 수정 404 — 캡처**

memberId는 <내회원ID>로 복구하고 ratingId를 `9223372036854775807`로 바꿔 PATCH를 보냅니다. 기존 평점이 아닌 ID이므로 404와 오류 메시지를 확인합니다.

**⑧ 평점 삭제 204 — 캡처**

DELETE /members/<내회원ID>/ratings/<평점ID>를 실행합니다. Code=204, Response body가 없는 것이 정상입니다. 이후 GET으로 해당 평점이 사라진 것도 확인합니다. 이번에 만든 평점만 삭제하고 기존 시드 평점을 삭제하지 마세요.

**선택 확인:** ③ 직후 같은 등록 요청을 반복하면 409 중복 평점 오류를 확인할 수 있습니다. Standard의 ‘실패 2가지 이상’은 ②·④로 충족하며, 중복 평점도 구현·자동 테스트했습니다.

## 코드 자료는 대표 파일 링크로 정리

GitHub에 6주차 코드를 올린 후 아래 대표 파일의 링크를 노션에 넣으면 됩니다. 이번 자료는 핵심 코드 ‘링크’를 명시하므로 코드 전체를 여러 장 캡처할 필요가 없습니다.

- 입력/응답: SignUpRequest, CreateRatingRequest, ApiResponse
- 저장 로직: MemberService, RatingService
- 오류 처리: GlobalExceptionHandler

API 명세는 위 표를 노션에 복사합니다. 필수 캡처 ①~⑧과 아래 트러블슈팅을 붙이면 Standard 증빙을 정리할 수 있습니다. 본인이 실행하지 않은 검증은 직접 수행한 것으로 적지 않습니다.

## 입력 처리 보완 사항

bcrypt의 입력 한도를 초과한 비밀번호는 UTF-8 72바이트 기준으로 검사해 400으로 응답합니다. 또한 Jackson이 소수 별점을 정수로 잘라 받지 않도록 `accept-float-as-int: false`를 설정했습니다.

공식 참고:

- [Spring MVC Validation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html)
- [Spring Security Password Storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)
- [BCrypt 입력 길이 검사 소스](https://github.com/spring-projects/spring-security/blob/main/crypto/src/main/java/org/springframework/security/crypto/bcrypt/BCrypt.java)
