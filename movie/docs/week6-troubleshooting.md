# 6주차 트러블슈팅: 조회 전용 CORS 설정으로 쓰기 요청 차단

## 이슈

6주차 회원가입·평점 쓰기 API를 추가한 뒤, 허용된 프론트 출처에서 POST/PATCH/DELETE 사전 요청을 통과하는지 자동 테스트했다. POST preflight가 200 대신 403을 반환했다.

실제 재현 명령:

```powershell
.\gradlew.bat test --tests '*Week6ApiTests.allowsWritePreflightsFromFrontend'
```

테스트에서 관찰한 응답:

```text
Origin: http://localhost:5173
Access-Control-Request-Method: POST
Access-Control-Request-Headers: content-type

Status = 403
Body = Invalid CORS request
Status expected:<200> but was:<403>
```

이 기록은 MockMvc 통합 테스트로 재현한 것으로, 사용자가 브라우저에서 직접 겪은 오류로 적은 것이 아니다.

## 원인

5주차 `WebConfig`에는 조회에 필요한 `.allowedMethods("GET")`만 설정되어 있었다. API 메서드를 추가하더라도 CORS 허용 메서드는 자동으로 확장되지 않기 때문에, 브라우저가 JSON 쓰기 요청 전에 보내는 사전 요청을 서버가 거절했다. Swagger는 백엔드와 같은 출처에서 실행되므로 Swagger 성공만으로 다른 출처의 브라우저 연동까지 검증했다고 볼 수 없다.

## 해결

프론트 출처 제한을 유지하면서 현재 API에 필요한 메서드만 추가했다.

```java
registry.addMapping("/members/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("GET", "POST", "PATCH", "DELETE")
        .allowedHeaders("*");
```

## 재검증 방법

```powershell
.\gradlew.bat test bootJar
```

`Week6ApiTests.allowsWritePreflightsFromFrontend`는 POST/PATCH/DELETE 사전 요청이 모두 200이고 지정한 Origin이 응답에 담기는지 확인한다. 기존 테스트는 허용하지 않은 Origin과 PUT이 여전히 차단되는지 검사한다. 최종 통과 결과는 `week6-verification.md`에 기록한다.

## 참고

[Spring MVC CORS 공식 문서](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)
