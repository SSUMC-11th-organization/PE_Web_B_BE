# 5주차 미션 기록 초안

## 설계와 구현

영화 정보는 TMDB가 제공하고, 자체 서버는 회원과 평점을 담당하도록 나눴다. Member : Rating, Member : Favorite은 각각 1:N이다. movieId는 외부 TMDB 식별자이므로 자체 Movie 테이블이나 외래키를 만들지 않았다. `(member_id, movie_id)` 복합 UNIQUE로 회원 한 명이 같은 영화를 중복 평가/즐겨찾기하지 못하도록 했다. Favorite은 스키마만 만들었다.

Spring Boot 3.5.0과 Java 21로 새 movie 프로젝트를 생성했다. Controller는 HTTP 요청과 입력 검증을, Service는 중복 여부 판단 및 회원 존재 확인을, Repository는 DB 조회를 담당한다. 읽기 전용 트랜잭션 안에서 Rating을 DTO로 변환하여 LAZY 회원 관계와 비밀번호가 JSON에 노출되지 않게 했다. 평점은 워크북과 동일하게 ratingId 내림차순이다. 수정일 기준 정렬은 아니다.

선택 미션 중 닉네임 2~12자, 이메일 형식 검증을 구현했다. 형식이 틀리면 DB 중복 검사 전에 400으로 응답한다. CORS도 현재 필요한 GET과 지정한 프론트 출처만 허용했다.

## 진행 순서와 증빙

아래 항목은 **실제 실행 후** 채운다. 자동 테스트 성공을 수동 검증 완료로 기록하지 않는다.

1. MySQL schema/seed 실행: [실행 결과와 테이블 캡처]
2. 핵심 코드: [Entity/DTO/Controller/Service 링크 또는 캡처]
3. Postman 닉네임 기존/신규: [URL + JSON + 상태 캡처]
4. Postman 이메일 기존/신규: [캡처]
5. 회원 1 평점 2건 내림차순 / 없는 회원 404: [캡처]
6. CORS 비활성화 상태에서 브라우저 실패: [Console + Network 캡처]
7. CORS 활성화 후 실제 화면 3개 API 성공: [화면 + 응답 캡처]
8. 선택 유효성 검증: [닉네임 a / 이메일 not-email → 400 캡처]

## 핵심 키워드 메모

- SOP는 프로토콜·호스트·포트를 기준으로 다른 출처의 응답을 JavaScript가 읽는 것을 제한한다. CORS 응답 헤더로 허용 출처를 알릴 수 있다. 단순 GET은 서버에 도착해도 브라우저에서 응답 읽기가 차단될 수 있고, 사전 요청이 필요한 경우 preflight 실패로 본 요청이 전송되지 않을 수도 있다.
- Same-Site는 현대 브라우저의 schemeful 기준에서 스킴과 등록 가능 도메인을 고려한다. 포트가 다른 localhost는 cross-origin일 수 있지만 same-site일 수 있다. 워크북의 ‘도메인만 같으면 된다’는 설명에는 스킴 조건을 보충해야 한다.
- SameSite 쿠키 속성은 cross-site 요청에서 쿠키 전송을 제한한다. Strict/Lax/None이 있으며 None에는 Secure가 필요하다. CORS 허용 자체가 인증이나 CSRF 방어를 대신하지 않는다.
- 복합 UNIQUE는 개별 컬럼의 중복은 허용하면서 두 값의 조합 중복을 DB에서 막는다. 애플리케이션 사전 중복 확인만 하면 동시 요청이 경쟁할 수 있다.
- 프론트 번들/브라우저 요청의 키는 사용자가 볼 수 있다. 비밀로 유지해야 하는 키는 백엔드 환경 변수에 보관한다. 프론트 직접 호출은 공급자가 공개 클라이언트 사용을 허용하는 키·권한·정책을 확인해야 한다.

## CORS 범위 비교 설명 초안 (선택 미션은 실제 두 출처 캡처 후 사용)

`*`를 허용하면 여러 출처의 브라우저 화면이 응답을 읽을 수 있다. 특정 Origin만 허용하면 지정하지 않은 출처에는 CORS 허용 헤더가 제공되지 않아 브라우저에서 응답 읽기가 차단된다. 이번 서비스는 개발 화면의 출처가 정해져 있어 localhost:5173과 필요한 GET으로 범위를 제한했다. 이는 서버 접근 권한을 통제하는 인증 기능과 다르며 Postman과 같은 비브라우저 클라이언트를 차단하지 않는다.

## 자료에서 주의할 부분

한글 경로를 큰따옴표로 감싸면 따옴표까지 닉네임에 포함되어 다른 값을 조회할 수 있다. 문자열 자체는 바꾸지 않고 URL 인코딩해야 한다. CORS 오류가 보이더라도 실제 URL의 404, 프록시 오류, 허용 Origin 오타 등을 Network에서 함께 확인한다. 워크북에 포함된 Spring Initializr 화면의 버전 대신 본문의 3.5.0 고정 요구를 사용했다.

## 트러블슈팅

- 이슈: [본인이 실제로 만난 오류]
- 원인: [로그/Network로 확인한 근거]
- 해결: [변경 사항과 재검증 결과]
- 캡처: [전/후]

## 본인 회고

[직접 이해한 내용, 아직 헷갈리는 내용, 다음 주차에 보완할 점을 작성]

## 공식 참고 자료

- [Spring MVC CORS](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)
- [MDN Same-origin policy](https://developer.mozilla.org/en-US/docs/Web/Security/Defenses/Same-origin_policy)
- [MDN Site](https://developer.mozilla.org/en-US/docs/Glossary/Site)
