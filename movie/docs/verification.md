# 구현 검증 결과

- 백엔드: `gradlew.bat test bootJar` 성공.
- `MemberApiTests`: 6개 성공. 중복/신규 닉네임·이메일, 잘못된 형식 400, 회원별 평점과 정렬/DTO, 빈 목록/없는 회원 404, CORS 허용·차단·preflight 검증.
- `CorsDisabledTests`: 1개 성공. 데이터 응답 200이면서 CORS 허용 헤더가 없는 상태 검증.
- 테스트 DB: H2 MySQL 호환 모드. 실제 MySQL 연결·스키마 validate는 별도 실행 필요.
- 기존 UMCine 5주차: `npm run build` 성공 (TypeScript + Vite).
- 브라우저에서 기존 UMCine의 3개 API 패널과 영화 목록이 함께 표시되는 것을 확인. `umcine-api-panel.png`는 응답 호출 전 배치 확인용이며 성공 응답 인증 캡처가 아님.
- 독립 실습 화면: inline JavaScript 구문 검증 성공.
- 사용자가 로컬 MySQL을 준비하고 서버를 실행했으며, MySQL 8.0.46 연결 및 애플리케이션 기동 로그를 확인함.
- 사용자가 제공한 Swagger 캡처에서 닉네임/이메일 중복 확인의 HTTP 200과 available=false를 확인함.
- 사용자가 제공한 기존 UMCine 캡처에서 세 API의 HTTP 200과 평점 2건(ratingId 2, 1 순서)을 확인함.
- Swagger의 회원별 평점 조회 캡처는 별도 제출 필요. Postman 수동 검증을 수행했다고 기록하지 않음.

초기 offline 테스트에서는 Spring Boot가 관리하는 H2 2.3.232가 캐시에 없어 실패했다. 온라인 의존성 해석 후 동일 테스트와 빌드가 성공했다. 비밀번호/접속 정보 없이 MySQL 결과를 성공으로 기록하지 않았다.
