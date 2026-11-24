// src/main/java/com/umc/movie/config/SwaggerExamples.java
package com.umc.movie.config;

/**
 * 여러 API가 함께 쓰는 실패 응답 예시입니다.
 * 상수 이름이 곧 응답 바디의 code라서, 문서에서 어떤 실패인지 바로 알아볼 수 있습니다.
 */
public final class SwaggerExamples {

    public static final String COMMON4001 = """
            {
              "isSuccess": false,
              "code": "COMMON4001",
              "message": "(어긴 검증 조건의 메시지가 담깁니다)",
              "result": null
            }
            """;

    public static final String AUTH401_2 = """
            {
              "isSuccess": false,
              "code": "AUTH401_2",
              "message": "로그인이 필요한 요청입니다.",
              "result": null
            }
            """;

    public static final String AUTH401_3 = """
            {
              "isSuccess": false,
              "code": "AUTH401_3",
              "message": "유효하지 않은 토큰입니다.",
              "result": null
            }
            """;

    public static final String AUTH401_4 = """
            {
              "isSuccess": false,
              "code": "AUTH401_4",
              "message": "토큰이 만료되었습니다. 다시 로그인해주세요.",
              "result": null
            }
            """;

    public static final String RATING403_1 = """
            {
              "isSuccess": false,
              "code": "RATING403_1",
              "message": "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.",
              "result": null
            }
            """;

    public static final String MEMBER404_1 = """
            {
              "isSuccess": false,
              "code": "MEMBER404_1",
              "message": "해당 회원을 찾을 수 없습니다.",
              "result": null
            }
            """;

    public static final String RATING404_1 = """
            {
              "isSuccess": false,
              "code": "RATING404_1",
              "message": "해당 평점을 찾을 수 없습니다.",
              "result": null
            }
            """;

    public static final String FAVORITE404_1 = """
            {
              "isSuccess": false,
              "code": "FAVORITE404_1",
              "message": "즐겨찾기한 영화를 찾을 수 없습니다.",
              "result": null
            }
            """;

    public static final String MEMBER409_1 = """
            {
              "isSuccess": false,
              "code": "MEMBER409_1",
              "message": "이미 사용 중인 닉네임입니다.",
              "result": null
            }
            """;

    public static final String RATING409_1 = """
            {
              "isSuccess": false,
              "code": "RATING409_1",
              "message": "이미 이 영화에 평점을 남기셨습니다.",
              "result": null
            }
            """;

    public static final String FAVORITE409_1 = """
            {
              "isSuccess": false,
              "code": "FAVORITE409_1",
              "message": "이미 이 영화를 즐겨찾기하셨습니다.",
              "result": null
            }
            """;

    private SwaggerExamples() {
    }
}
